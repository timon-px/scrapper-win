package com.desktop.core.api;

import com.desktop.core.api.interfaces.IUniqueizerService;
import com.desktop.core.common.dto.UniqueizerRequestDTO;
import com.desktop.core.common.dto.UniqueizerResponseDTO;
import com.desktop.core.scrapper.processor.interfaces.IDocumentProcess;
import com.desktop.core.storage.IStorageWorker;
import com.desktop.core.storage.StorageWorker;
import com.desktop.core.uniqueizer.UniqueizerWorker;
import com.desktop.core.uniqueizer.processor.UniqueizerProcessor;
import com.desktop.core.utils.DocumentWorker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UniqueizerService implements IUniqueizerService {
    private static final Logger log = LoggerFactory.getLogger(UniqueizerService.class);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    public CompletableFuture<UniqueizerResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest) {
        IStorageWorker storageWorker = new StorageWorker(uniqueizerRequest.getSavePath());
        List<File> files = validateAndGetFiles(uniqueizerRequest);

        if (files.isEmpty()) {
            return CompletableFuture.completedFuture(
                    new UniqueizerResponseDTO(false, "No valid files provided")
            );
        }

        return processFiles(files, uniqueizerRequest.getProcessingOptions(), storageWorker);
    }

    // Input validation
    private List<File> validateAndGetFiles(UniqueizerRequestDTO request) {
        List<File> files = request.getFiles();
        if (files == null)
            return Collections.emptyList();

        return files.stream()
                .filter(Objects::nonNull)
                .filter(File::exists)
                .filter(File::canRead)
                .toList();
    }

    // Main processing orchestration
    private CompletableFuture<UniqueizerResponseDTO> processFiles(List<File> files,
                                                                  UniqueizerRequestDTO.ProcessingOptions processingOptions,
                                                                  IStorageWorker storageWorker) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(files.size(), Runtime.getRuntime().availableProcessors())
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<SimpleDoubleProperty> fileProgresses = bindProgresses(files);

                List<CompletableFuture<Path>> processingFutures = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    SimpleDoubleProperty fileProgress = fileProgresses.get(i);
                    processingFutures.add(processSingleFile(file, processingOptions, storageWorker, fileProgress));
                }

                return aggregateResults(processingFutures);
            } finally {
                executor.shutdown();
            }
        }, executor);
    }

    // Process individual file
    private CompletableFuture<Path> processSingleFile(File file,
                                                      UniqueizerRequestDTO.ProcessingOptions processingOptions,
                                                      IStorageWorker storageWorker,
                                                      SimpleDoubleProperty progress) {
        try {
            Document document = parseFile(file);
            return processAndSaveDocument(document, file, processingOptions, storageWorker, progress);
        } catch (IOException e) {
            log.error("Failed to process file: {}", file.getAbsolutePath());
            return null;
        }
    }

    // Parse file to document
    private Document parseFile(File file) throws IOException {
        return Jsoup.parse(file);
    }

    // Process document and save
    private CompletableFuture<Path> processAndSaveDocument(Document document,
                                                           File file,
                                                           UniqueizerRequestDTO.ProcessingOptions processingOptions,
                                                           IStorageWorker storageWorker,
                                                           SimpleDoubleProperty progress) {
        return startProcesses(document, processingOptions, progress)
                .thenCompose(unused ->
                        storageWorker.SaveContentAsync(document.outerHtml(),
                                UniqueizerWorker.GetUniqueizerFileName(file)));
    }

    // Aggregate processing results
    private UniqueizerResponseDTO aggregateResults(List<CompletableFuture<Path>> processingFutures) {
        CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0])).join();
        List<Path> successfulPaths = processingFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .map(Path::getParent).map(Path::toAbsolutePath)
                .toList();

        return createResponse(successfulPaths);
    }

    // Create response object
    private UniqueizerResponseDTO createResponse(List<Path> successfulPaths) {
        String message = successfulPaths.isEmpty()
                ? "No files were processed successfully"
                : "Websites have been successfully unified!\nProcessed " + successfulPaths.size() + " files";

        return new UniqueizerResponseDTO(
                !successfulPaths.isEmpty(),
                successfulPaths,
                message
        );
    }

    // Bind progresses for several documents
    private List<SimpleDoubleProperty> bindProgresses(List<File> files) {
        List<SimpleDoubleProperty> fileProgresses = files.stream()
                .map(file -> new SimpleDoubleProperty(0.0))
                .toList();

        DocumentWorker.BindOverallProgress(progress, fileProgresses);
        return fileProgresses;
    }


    private CompletableFuture<Void> startProcesses(Document document,
                                                   UniqueizerRequestDTO.ProcessingOptions processingOptions,
                                                   SimpleDoubleProperty progress) {
        IDocumentProcess uniqueizerProcessor = new UniqueizerProcessor(processingOptions);
        return uniqueizerProcessor.ProcessAsync(document, progress);
    }
}
