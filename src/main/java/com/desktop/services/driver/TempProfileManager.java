package com.desktop.services.driver;

import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class TempProfileManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(TempProfileManager.class);
    private final Path tempProfileDir;

    public TempProfileManager(File configPath) throws IOException {
        this.tempProfileDir = Files.createTempDirectory("scrapper-temp-profile");
        log.info("Temporary folder has been created: {}", tempProfileDir);

        if (configPath == null || !configPath.exists()) {
            log.error("Config path does not exist");
            return;
        }

        copyDirectory(configPath.toPath(), tempProfileDir);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteDirectory(tempProfileDir);
                log.info("Temporary folder has been removed with shutdown hook: {}", tempProfileDir);
            } catch (IOException e) {
                log.error("Error while folder removing with shutdown hook: {}", e.getMessage());
            }
        }));
    }

    public void applyToOptions(ChromeOptions options) {
        options.addArguments("user-data-dir=" + tempProfileDir.toString());
    }

    @Override
    public void close() throws IOException {
        if (Files.exists(tempProfileDir)) {
            deleteDirectory(tempProfileDir);
            log.info("Temporary folder has been removed: {}", tempProfileDir);
        }
    }

    // Copy directory
    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(src -> {
                try {
                    Path dest = target.resolve(source.relativize(src));
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error while folder copying: " + e.getMessage(), e);
                }
            });
        }
    }

    // Remove directory
    private void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Error while folder removing: " + e.getMessage(), e);
                        }
                    });
        }
    }
}
