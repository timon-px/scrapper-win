package com.desktop.application.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class FileExplorerUtils {
    private static final Logger log = LoggerFactory.getLogger(FileExplorerUtils.class);

    public static CompletableFuture<Void> OpenFolderAsync(Path folderPath) {
        return CompletableFuture.runAsync(() -> {
            File folder = folderPath.toFile();

            // Validate folder
            if (!folder.exists()) {
                log.error("Folder does not exist: {}", folderPath);
                throw new IllegalArgumentException("Folder does not exist: " + folderPath);
            }
            if (!folder.isDirectory()) {
                log.error("Path is not a directory: {}", folderPath);
                throw new IllegalArgumentException("Path is not a directory: " + folderPath);
            }
            if (!folder.canRead()) {
                log.error("No read permission for folder: {}", folderPath);
                throw new SecurityException("No read permission for folder: " + folderPath);
            }

            log.info("Attempting to open folder: {}", folderPath);

            // Try Desktop API
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desktop.open(folder);
                        return;
                    } catch (IOException e) {
                        log.warn("Desktop API failed to open folder: {}", folderPath, e);
                    } catch (Exception e) {
                        log.warn("Unexpected error with Desktop API: {}", folderPath, e);
                    }
                } else {
                    log.warn("Desktop.Action.OPEN not supported on this system");
                }
            } else {
                log.warn("Desktop API not supported on this system");
            }

            // Fallback to OS-specific commands
            String os = System.getProperty("os.name").toLowerCase();
            try {
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("explorer", folder.getAbsolutePath());
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", folder.getAbsolutePath());
                } else if (os.contains("nix") || os.contains("nux")) {
                    pb = new ProcessBuilder("xdg-open", folder.getAbsolutePath());
                } else {
                    log.error("Unsupported OS: {}", os);
                    throw new UnsupportedOperationException("Cannot open folder on this OS: " + os);
                }

                log.debug("Executing command: {}", String.join(" ", pb.command()));
                Process process = pb.start();

                // Log process exit code asynchronously
                CompletableFuture.runAsync(() -> {
                    try {
                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            log.warn("Command failed with exit code {}: {}", exitCode, folderPath);
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting for process exit", e);
                    }
                });
            } catch (IOException e) {
                log.error("Failed to execute fallback command for folder: {}", folderPath, e);
                throw new RuntimeException("Failed to open folder: " + folderPath, e);
            }
        });
    }
}
