package com.desktop.services.services.classes;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.services.models.FileSaveModel;
import com.desktop.services.processors.FilesProcess;
import com.desktop.services.processors.interfaces.IDocumentProcess;
import com.desktop.services.processors.interfaces.IFilesProcess;
import com.desktop.services.processors.scrapper.HtmlProcessor;
import com.desktop.services.processors.scrapper.StylesheetProcessor;
import com.desktop.services.services.interfaces.IScrapperService;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.storage.StorageWorker;
import com.desktop.services.utils.DocumentWorker;
import com.desktop.services.utils.ScrapperWorker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
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
                request.getDirectory() != null && request.getDirectory().toFile().exists() && request.getDirectory().toFile().isDirectory();
    }

    // Main async processing
    private CompletableFuture<ScrapperResponseDTO> processWebAsync(ScrapperRequestDTO request, IStorageWorker storageWorker) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetchDocument(request);
                    } catch (IOException e) {
                        throw new RuntimeException("Error during initial web scraping setup: " + e.getMessage(), e);
                    }
                }, executor)
                .thenCompose(document -> {
                    ConcurrentHashMap<String, FileSaveModel> filesToSaveList = new ConcurrentHashMap<>();

                    updateProgress(0.05); // Initial fetch complete
                    try {
                        Path savePath = prepareSavePath(document, storageWorker);
                        return processDocument(document, savePath, filesToSaveList, storageWorker, request.getProcessingOptions());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Error occurred while web parsing: " + e.getMessage(), e);
                    }
                })
                .thenApply(this::createSuccessResponse)
                .exceptionally(this::handleProcessingError)
                .whenComplete((result, throwable) -> executor.shutdown()); // Shutdown executor when done
    }

    // Process document and save results
    private CompletableFuture<Path> processDocument(
            Document document,
            Path savePath,
            ConcurrentHashMap<String, FileSaveModel> filesToSaveList,
            IStorageWorker storageWorker,
            ScrapperRequestDTO.ProcessingOptions options) {

        return startProcesses(document, savePath, filesToSaveList, storageWorker)
                .thenCompose(unused -> {
                    finalProcess(document, options);
                    return saveDocument(document, savePath, storageWorker);
                })
                .thenApply(finalPath -> {
                    updateProgress(1.0); // Complete
                    return finalPath.getParent();
                });
    }

    // Start processing chain
    private CompletableFuture<Void> startProcesses(
            Document document,
            Path path,
            ConcurrentHashMap<String, FileSaveModel> filesToSaveList,
            IStorageWorker storageWorker) {

        IDocumentProcess stylesheetProcessor = new StylesheetProcessor(storageWorker, path, filesToSaveList);
        IDocumentProcess htmlProcessor = new HtmlProcessor(filesToSaveList);
        IFilesProcess filesProcessor = new FilesProcess(storageWorker, path);

        return stylesheetProcessor.ProcessAsync(document, progress)
                .thenCompose(unused -> htmlProcessor.ProcessAsync(document, progress))
                .thenCompose(unused -> filesProcessor.SaveAsync(filesToSaveList, progress));
    }

    // Fetch the document from URL
    private Document fetchDocument(ScrapperRequestDTO request) throws IOException {
        return ScrapperWorker.GetDocument(request.getUrl(), request.getProcessingOptions());
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

    // Save the final document
    private CompletableFuture<Path> saveDocument(Document document, Path path, IStorageWorker storageWorker) {
        return storageWorker.SaveContentAsync(document.outerHtml(), path, ScrapperConstants.HTML_NAME);
    }

    // Create success response
    private ScrapperResponseDTO createSuccessResponse(Path finalPath) {
        return new ScrapperResponseDTO(true, finalPath.toAbsolutePath(), "Website has successfully parsed!");
    }

    // Handle errors during processing
    private ScrapperResponseDTO handleProcessingError(Throwable ex) {
        log.error("Error during web processing", ex);
        return new ScrapperResponseDTO(false, "Error occurred while processing:\n" + ex.getMessage());
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