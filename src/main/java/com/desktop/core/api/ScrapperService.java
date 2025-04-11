package com.desktop.core.api;

import com.desktop.core.api.interfaces.IScrapperService;
import com.desktop.core.common.constants.ScrapperConstants;
import com.desktop.core.common.dto.ScrapperRequestDTO;
import com.desktop.core.common.dto.ScrapperResponseDTO;
import com.desktop.core.common.model.FileSaveModel;
import com.desktop.core.common.model.ProcessingContext;
import com.desktop.core.scrapper.ScrapperWorker;
import com.desktop.core.scrapper.processor.FilesProcessor;
import com.desktop.core.scrapper.processor.HtmlProcessor;
import com.desktop.core.scrapper.processor.StylesheetProcessor;
import com.desktop.core.scrapper.processor.interfaces.IDocumentProcess;
import com.desktop.core.scrapper.processor.interfaces.IFilesProcess;
import com.desktop.core.storage.IStorageWorker;
import com.desktop.core.storage.StorageWorker;
import com.desktop.core.utils.DocumentWorker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScrapperService implements IScrapperService {
    private static final Logger log = LoggerFactory.getLogger(ScrapperService.class);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    public CompletableFuture<ScrapperResponseDTO> GetWeb(ScrapperRequestDTO scrapperRequest) {
        IStorageWorker storageWorker = createStorageWorker(scrapperRequest.getDirectory());
        resetProgress();

        if (!validateRequest(scrapperRequest)) {
            return CompletableFuture.completedFuture(
                    new ScrapperResponseDTO(false, "Invalid request: URL or directory is missing")
            );
        }

        return processWebAsync(scrapperRequest, storageWorker);
    }

    // Validate request input
    private boolean validateRequest(ScrapperRequestDTO request) {
        return request != null &&
                request.getUrl() != null && !request.getUrl().isEmpty() &&
                request.getDirectory() != null && request.getDirectory().toFile().exists() &&
                request.getDirectory().toFile().isDirectory();
    }

    // Main async processing
    private CompletableFuture<ScrapperResponseDTO> processWebAsync(ScrapperRequestDTO request, IStorageWorker storageWorker) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return CompletableFuture.supplyAsync(() -> fetchDocuments(request), executor)
                .thenCompose(documents -> processDocuments(documents, storageWorker, request))
                .exceptionally(this::handleProcessingError)
                .whenComplete((result, throwable) -> executor.shutdown()); // Shutdown executor when done
    }

    private CompletableFuture<ScrapperResponseDTO> processDocuments(List<Document> documents,
                                                                    IStorageWorker storageWorker,
                                                                    ScrapperRequestDTO request) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Document firstDocument = documents.getFirst();
                Path savePath = prepareSavePath(firstDocument, storageWorker);
                List<SimpleDoubleProperty> documentProgresses = bindProgresses(documents);

                List<CompletableFuture<Path>> processingFutures = new ArrayList<>();
                for (int i = 0; i < documents.size(); i++) {
                    Document document = documents.get(i);
                    SimpleDoubleProperty documentProgress = documentProgresses.get(i);
                    ProcessingContext processingContext = new ProcessingContext(savePath, documentProgress, request.getProcessingOptions());

                    processingFutures.add(processDocument(document, processingContext, i, storageWorker));
                }

                return aggregateResults(processingFutures, savePath);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Error occurred while web page processing: " + e.getMessage(), e);
            }
        });
    }

    // Process document and save results
    private CompletableFuture<Path> processDocument(
            Document document,
            ProcessingContext processingContext,
            int pageIndex,
            IStorageWorker storageWorker) {

        return startProcesses(document, processingContext, storageWorker)
                .thenCompose(unused -> {
                    finalProcess(document, processingContext.getOptions());
                    return saveDocument(document, processingContext.getSavePath(), pageIndex, storageWorker);
                })
                .thenApply(finalPath -> {
                    updateProgress(1.0); // Complete
                    return finalPath;
                });
    }

    // Start processing chain
    private CompletableFuture<Void> startProcesses(
            Document document,
            ProcessingContext processingContext,
            IStorageWorker storageWorker) {

        Path savePath = processingContext.getSavePath();
        ConcurrentHashMap<String, FileSaveModel> filesToSaveList = processingContext.getFilesToSaveList();
        ConcurrentHashMap<String, String> usedFileNamesCssList = processingContext.getUsedFileNamesCssList();
        SimpleDoubleProperty currentProgress = processingContext.progressProperty();

        IDocumentProcess stylesheetProcessor = new StylesheetProcessor(storageWorker, savePath, usedFileNamesCssList, filesToSaveList);
        IDocumentProcess htmlProcessor = new HtmlProcessor(filesToSaveList);
        IFilesProcess filesProcessor = new FilesProcessor(storageWorker, savePath);

        return stylesheetProcessor.ProcessAsync(document, currentProgress)
                .thenCompose(unused -> htmlProcessor.ProcessAsync(document, currentProgress))
                .thenCompose(unused -> filesProcessor.SaveAsync(filesToSaveList, currentProgress));
    }

    // Save the final document
    private CompletableFuture<Path> saveDocument(Document document, Path path, int pageIndex, IStorageWorker storageWorker) {
        String fileIdName = pageIndex > 0 ? "_" + pageIndex : "";
        String fileName = ScrapperConstants.HTML_NAME + fileIdName
                + ScrapperConstants.HTML_EXTENSION;

        return storageWorker.SaveContentAsync(document.outerHtml(), path, fileName);
    }

    // Aggregate processing results
    private ScrapperResponseDTO aggregateResults(List<CompletableFuture<Path>> processingFutures, Path savePath) {
        CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0])).join();
        List<Path> successfulPaths = processingFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .map(Path::toAbsolutePath)
                .toList();

        return createResponse(successfulPaths, savePath);
    }

    // Fetch the document from URL
    private List<Document> fetchDocuments(ScrapperRequestDTO request) {
        try {
            List<Document> documents = ScrapperWorker.GetDocuments(request.getUrl(), request.getProcessingOptions());
            updateProgress(0.05); // Initial fetch complete

            return documents;
        } catch (IOException e) {
            throw new RuntimeException("Error during web fetching: " + e.getMessage(), e);
        }
    }

    // Prepare save path based on document host
    private Path prepareSavePath(Document document, IStorageWorker storageWorker) throws URISyntaxException {
        String fileHost = new URI(document.location()).getHost();
        return storageWorker.GetFolderPath(fileHost);
    }

    // Final document modifications
    private void finalProcess(Document document, ScrapperRequestDTO.ProcessingOptions options) {
        document.select("base").remove();
        if (options.shouldReplaceHref()) {
            DocumentWorker.ReplaceAnchorHref(document, "{offer}");
        }
    }

    // Create success response
    private ScrapperResponseDTO createResponse(List<Path> successfulPaths, Path savePath) {
        String pluralPage = successfulPaths.size() == 1 ? " page" : " pages";
        String message = successfulPaths.isEmpty()
                ? "No pages were processed successfully"
                : "Website has been successfully parsed!\nProcessed " + successfulPaths.size() + pluralPage;

        return new ScrapperResponseDTO(
                !successfulPaths.isEmpty(),
                savePath,
                message
        );
    }

    // Handle errors during processing
    private ScrapperResponseDTO handleProcessingError(Throwable ex) {
        log.error("Error during web processing", ex);
        return new ScrapperResponseDTO(false, "Error occurred while processing:\n" + ex.getMessage());
    }

    // Bind progresses for several documents
    private List<SimpleDoubleProperty> bindProgresses(List<Document> documents) {
        List<SimpleDoubleProperty> documentProgresses = documents.stream()
                .map(file -> new SimpleDoubleProperty(0.0))
                .toList();

        DocumentWorker.BindOverallProgress(progress, documentProgresses);
        return documentProgresses;
    }

    // Update progress helper
    private void updateProgress(double value) {
        DocumentWorker.UpdateProgress(progress, value);
    }

    // Factory method for storage worker
    private IStorageWorker createStorageWorker(Path directory) {
        return new StorageWorker(directory);
    }

    // Reset progress to 0
    private void resetProgress() {
        progress.set(0.0);
    }
}