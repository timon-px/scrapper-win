package com.desktop.core.driver;

import com.desktop.core.common.constants.DriverConstants;
import com.desktop.core.driver.interfaces.IDriverOptionsManager;
import com.desktop.core.utils.ResourceExtractor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class TempProfileManager implements AutoCloseable, IDriverOptionsManager {
    private static final Logger log = LoggerFactory.getLogger(TempProfileManager.class);
    private final Path tempProfileDir;

    public TempProfileManager() throws IOException {
        this.tempProfileDir = Files.createTempDirectory("scrapper-temp-profile");
        log.info("Temporary folder has been created: {}", tempProfileDir);

        ResourceExtractor.ExtractDirectory(DriverConstants.DRIVER_PROFILE_PATH, tempProfileDir);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteDirectory(tempProfileDir);
                log.info("Temporary folder has been removed with shutdown hook: {}", tempProfileDir);
            } catch (IOException e) {
                log.error("Error while folder removing with shutdown hook: {}", e.getMessage());
            }
        }));
    }

    @Override
    public void ApplyToOptions(ChromeOptions options) {
        options.addArguments("user-data-dir=" + tempProfileDir.toString());
    }

    @Override
    public void close() throws IOException {
        if (Files.exists(tempProfileDir)) {
            deleteDirectory(tempProfileDir);
            log.info("Temporary folder has been removed: {}", tempProfileDir);
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
