package com.desktop.services.driver;

import com.desktop.services.models.DriverSaveModel;
import com.google.common.base.Strings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
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
        return RunWebDriver(false);
    }

    public CompletableFuture<List<DriverSaveModel>> RunWebDriver(boolean shouldProcessStyles) {
        return CompletableFuture.supplyAsync(this::initializeDriver, EXECUTOR)
                .thenCompose(driver -> waitForDriverToClose(driver, shouldProcessStyles));
    }

    private DriverWithProfile initializeDriver() {
        try {
            ChromeOptions options = JavaScriptDriver.GetChromeOptions();

            TempProfileManager profileManager = new TempProfileManager();
            profileManager.applyToOptions(options);

            WebDriver driver = new ChromeDriver(options);
            driver.get(url);
            driver.manage().window().maximize();

            return new DriverWithProfile(driver, profileManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize WebDriver: " + e.getMessage(), e);
        }
    }

    private CompletableFuture<List<DriverSaveModel>> waitForDriverToClose(DriverWithProfile driverWithProfile,
                                                                          boolean shouldProcessStyles) {
        return CompletableFuture.supplyAsync(() -> {
            WebDriver driver = driverWithProfile.getDriver();

            CapturePageWorker capturePageWorker = new CapturePageWorker(shouldProcessStyles);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            JavaScriptDriver javaScriptDriver = new JavaScriptDriver(js);

            try {
                waitForInitialLoad(driver);
                pollAndCapture(driver, javaScriptDriver, capturePageWorker);
            } catch (Exception e) {
                log.error("Processing error", e);
            } finally {
                closeDriver(driverWithProfile);
            }

            return capturePageWorker.GetSaveModelList();
        }, EXECUTOR);
    }

    private void pollAndCapture(WebDriver driver,
                                JavaScriptDriver javaScriptDriver,
                                CapturePageWorker capturePageWorker) {
        String lastHtml = null;
        while (true) {
            try {
                lastHtml = driver.getPageSource();

                if (javaScriptDriver.IsReadyToSave()) {
                    capturePageWorker.CapturePage(lastHtml, javaScriptDriver);
                    javaScriptDriver.SetSavedHtml(capturePageWorker.GetAmount());
                }

                Thread.sleep(POLL_INTERVAL);
            } catch (NoSuchSessionException e) {
                log.info("Browser closed by user, stopping polling");
                captureFinalState(lastHtml, javaScriptDriver, capturePageWorker);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Polling interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Polling error", e);
                captureFinalState(lastHtml, javaScriptDriver, capturePageWorker);
                break;
            }
        }
    }

    private void waitForInitialLoad(WebDriver driver) {
        new WebDriverWait(driver, INITIAL_WAIT_TIMEOUT)
                .until(wd -> !Strings.isNullOrEmpty(wd.getTitle()));
    }

    private void captureFinalState(String finalHtml,
                                   JavaScriptDriver javaScriptDriver,
                                   CapturePageWorker capturePageWorker) {

        log.info("Try to capture last html state if doesn't before");

        try {
            capturePageWorker.CaptureFinalPage(finalHtml, javaScriptDriver);
        } catch (Exception e) {
            log.warn("Failed to capture final HTML, browser may be closed: {}", e.getMessage());
        }
    }

    private void closeDriver(DriverWithProfile driverWithProfile) {
        try {
            driverWithProfile.close();
        } catch (Exception e) {
            log.warn("Failed to quit driver, may already be closed", e);
        }
    }

    // Optional: Shutdown hook to clean up executor on JVM exit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));
    }
}