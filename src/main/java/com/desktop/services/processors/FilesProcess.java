package com.desktop.services.processors;

import com.desktop.services.models.FileSaveModel;
import com.desktop.services.processors.interfaces.IFilesProcess;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.utils.DocumentWorker;
import javafx.beans.property.DoubleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FilesProcess implements IFilesProcess {
    private static final Logger log = LoggerFactory.getLogger(FilesProcess.class);

    private final IStorageWorker storageWorker;
    private final Path mainPath;

    public FilesProcess(IStorageWorker storageWorker, Path mainPath) {
        this.storageWorker = storageWorker;
        this.mainPath = mainPath;
    }

    @Override
    public CompletableFuture<Void> SaveAsync(ConcurrentHashMap<String, FileSaveModel> filesToSave, DoubleProperty progress) {
        if (filesToSave.isEmpty()) return CompletableFuture.completedFuture(null);

        try {
            storageWorker.InitFoldersAsync(filesToSave.values(), mainPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        double baseProgress = progress.get() + 0.05;
        DocumentWorker.UpdateProgress(progress, baseProgress);

        List<CompletableFuture<String>> downloadFutures = initAsyncDownloads(filesToSave, progress, baseProgress);

        // Wait for all downloads to complete
        return CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    log.error("Error during downloads: {}", throwable.getMessage());
                    return null;
                });
    }

    private List<CompletableFuture<String>> initAsyncDownloads(ConcurrentHashMap<String, FileSaveModel> filesToSave, DoubleProperty progress, double baseProgress) {
        int totalFiles = filesToSave.size();
        AtomicInteger completedFiles = new AtomicInteger(0);

        double downloadWeight = 1 - baseProgress;
        List<CompletableFuture<String>> downloadFutures = new ArrayList<>();

        // Iterate over the map and start async downloads
        for (var entry : filesToSave.entrySet()) {
            String url = entry.getKey();
            FileSaveModel fileModel = entry.getValue();

            CompletableFuture<String> future = storageWorker.SaveFileAsync(url, fileModel, mainPath);
            future.thenRun(() -> {
                int completed = completedFiles.incrementAndGet();
                double fileProgress = DocumentWorker.GetProgressIncrement(completed, totalFiles * downloadWeight);
                DocumentWorker.UpdateProgress(progress, fileProgress + baseProgress);
            }).exceptionally(throwable -> {
                log.error("Download failed: {}", throwable.getMessage());
                return null;
            });

            downloadFutures.add(future);
        }

        return downloadFutures;
    }
}