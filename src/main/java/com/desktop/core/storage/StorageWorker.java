package com.desktop.core.storage;

import com.desktop.core.common.enums.SaveAsEnum;
import com.desktop.core.common.model.FileSaveModel;
import com.desktop.core.scrapper.PathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StorageWorker implements IStorageWorker {
    private static final Logger log = LoggerFactory.getLogger(StorageWorker.class);

    private final Path baseLocation;
    private final HttpClient httpClient;

    public StorageWorker(Path location) {
        this.baseLocation = location;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public Path GetFolderPath(String initName) {
        String folderName = initName + "_" + System.currentTimeMillis();
        Path folderPath = baseLocation.resolve(folderName);
        createDirectoriesIfNotExists(folderPath);
        return folderPath;
    }

    @Override
    public CompletableFuture<Path> SaveContentAsync(String content, String fileName) {
        return SaveContentAsync(content, baseLocation, fileName);
    }

    @Override
    public CompletableFuture<Path> SaveContentAsync(String content, Path folderPath, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            Path filePath = folderPath.resolve(fileName);
            try {
                createDirectoriesIfNotExists(folderPath);
                Files.writeString(filePath, content);
                return filePath;
            } catch (IOException e) {
                throw new StorageException("Failed to save file: " + filePath, e);
            }
        });
    }

    @Override
    public CompletableFuture<String> SaveFileAsync(String url, FileSaveModel fileSaveModel, Path mainPath) {
        try {
            // Define the output file path based on uniqueName and fileType
            String fileName = fileSaveModel.getUniqueName();
            SaveAsEnum fileType = fileSaveModel.getFileType();

            Path filePath = PathHelper.GetPath(fileType, mainPath.toString(), fileName);
            createDirectoriesIfNotExists(filePath.getParent());

            // Download the file
            URI fileUrl = new URI(url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(fileUrl)
                    .timeout(java.time.Duration.ofSeconds(30)) // Per-request timeout
                    .GET()
                    .build();

            return sendRequestAsync(request, filePath, url);
        } catch (Exception e) {
            log.error("Download failed for {}: {}", url, e.getMessage());
            return CompletableFuture.failedFuture(new Exception("Failed to download " + url + ": " + e.getMessage()));
        }
    }

    @Override
    public void InitFoldersAsync(Collection<FileSaveModel> fileSaveModels, Path mainPath) {
        CompletableFuture.runAsync(() -> fileSaveModels.parallelStream().forEach(model -> {
            Path path = PathHelper.GetPath(model.getFileType(), mainPath.toString());
            createDirectoriesIfNotExists(path);
        })).join();
    }

    private CompletableFuture<String> sendRequestAsync(HttpRequest request, Path destination, String url) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try (var inputStream = response.body()) {
                            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Downloaded: {} to {}", url, destination);
                            return "Downloaded: " + url;
                        } catch (Exception e) {
                            log.error("Failed to save file: {}", url);
                            throw new RuntimeException("Failed to save file: " + url, e);
                        }
                    } else {
                        log.error("HTTP error: {} for {}", response.statusCode(), url);
                        throw new RuntimeException("HTTP error: " + response.statusCode() + " for " + url);
                    }
                }).exceptionally(throwable -> "Failed: " + url + " - " + throwable.getMessage());
    }

    private synchronized void createDirectoriesIfNotExists(Path path) {
        try {
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create directory: " + path, e);
        }
    }
}
