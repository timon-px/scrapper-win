package com.desktop.services.driver;

import com.desktop.services.models.DriverSaveModel;
import com.desktop.services.utils.UniqueizerWorker;
import com.google.common.base.Strings;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScrapperDriver {
    private static final Logger log = LoggerFactory.getLogger(ScrapperDriver.class);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);
    private static final Duration INITIAL_WAIT_TIMEOUT = Duration.ofSeconds(5);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(); // Shared thread pool

    private final String url;

    public ScrapperDriver(String url) {
        this.url = Objects.requireNonNull(url, "URL must not be null");
    }

    public CompletableFuture<List<DriverSaveModel>> RunWebDriver() {
        return RunWebDriver(UniqueizerWorker.GetRandomCharString(12), false);
    }

    public CompletableFuture<List<DriverSaveModel>> RunWebDriver(String identifier, boolean shouldProcessStyles) {
        Objects.requireNonNull(identifier, "Identifier must not be null");
        return CompletableFuture.supplyAsync(this::initializeDriver, EXECUTOR)
                .thenCompose(driver -> waitForDriverToClose(driver, identifier, shouldProcessStyles))
                .exceptionally(throwable -> {
                    log.error("Error during web driver execution", throwable);
                    throw new RuntimeException("Web driver execution failed", throwable);
                });
    }

    private WebDriver initializeDriver() {
        try {
            WebDriver driver = new ChromeDriver();
            driver.get(url);
            driver.manage().window().maximize();
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    private CompletableFuture<List<DriverSaveModel>> waitForDriverToClose(WebDriver driver,
                                                                          String identifier,
                                                                          boolean shouldProcessStyles) {
        return CompletableFuture.supplyAsync(() -> {
            CapturePageWorker capturePageWorker = new CapturePageWorker(shouldProcessStyles);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            JavaScriptDriver javaScriptDriver = null;
            String lastHtml = null;

            try {
                waitForInitialLoad(driver);
                javaScriptDriver = new JavaScriptDriver(js, identifier);
                javaScriptDriver.Execute();

                while (true) {
                    try {
                        lastHtml = driver.getPageSource();
                        if (javaScriptDriver.IsReadyToSave()) {
                            capturePageWorker.CapturePage(lastHtml, javaScriptDriver);
                            javaScriptDriver.SetSavedHtml();
                        }

                        Thread.sleep(POLL_INTERVAL);
                    } catch (NoSuchSessionException e) {
                        log.info("Browser closed by user, stopping polling");
                        break;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Polling interrupted", e);
                        break;
                    } catch (Exception e) {
                        log.error("Polling error", e);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Processing error", e);
            } finally {
                lastHtml = captureFinalHtml(driver, lastHtml);
                capturePageWorker.CaptureFinalPage(lastHtml, javaScriptDriver);
                closeDriver(driver);
            }

            return capturePageWorker.GetSaveModelList();
        }, EXECUTOR);
    }

    private void waitForInitialLoad(WebDriver driver) {
        new WebDriverWait(driver, INITIAL_WAIT_TIMEOUT)
                .until(wd -> !Strings.isNullOrEmpty(wd.getTitle()));
    }

    private String captureFinalHtml(WebDriver driver, String lastHtml) {
        if (lastHtml != null) return lastHtml;
        try {
            return driver.getPageSource();
        } catch (Exception e) {
            log.warn("Failed to capture final HTML, browser may be closed", e);
            return null;
        }
    }

    private void closeDriver(WebDriver driver) {
        try {
            driver.quit();
        } catch (Exception e) {
            log.warn("Failed to quit driver, may already be closed", e);
        }
    }

    // Optional: Shutdown hook to clean up executor on JVM exit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));
    }
}
