package com.desktop.core.driver;

import com.desktop.core.common.model.DriverSaveModel;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
    private static final Duration INITIAL_WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(); // Shared thread pool

    private final String url;
    private final DriverProxyManager driverProxyManager;

    public ScrapperDriver(String url) {
        this(url, null);
    }

    public ScrapperDriver(String url, String proxyHost, int proxyPort) {
        this(url, String.format("%s:%s", proxyHost, proxyPort));
    }

    public ScrapperDriver(String url, String proxy) {
        this.url = Objects.requireNonNull(url, "URL must not be null");
        this.driverProxyManager = new DriverProxyManager(proxy);
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
            profileManager.ApplyToOptions(options);
            driverProxyManager.ApplyToOptions(options);

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
                startCapturing(driver, javaScriptDriver, capturePageWorker);
            } catch (Exception e) {
                log.error("Processing error", e);
            } finally {
                closeDriver(driverWithProfile);
            }

            return capturePageWorker.GetSaveModelList();
        }, EXECUTOR);
    }

    private void startCapturing(WebDriver driver,
                                JavaScriptDriver javaScriptDriver,
                                CapturePageWorker capturePageWorker) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, INITIAL_WAIT_TIMEOUT);
            wait.until(ExpectedConditions.not(innerDriver -> {
                try {
                    if (innerDriver == null) return false;
                    innerDriver.getTitle();

                    capturePage(driver, javaScriptDriver, capturePageWorker);
                    return true;
                } catch (Exception ex) {
                    log.info("Couldn't connect / Browser closed by user, stopping polling");
                    capturePageWorker.CaptureFinalPage();
                    return false;
                }
            }));
        } catch (org.openqa.selenium.TimeoutException ex) {
            log.info("Timeout | Trying again");
            startCapturing(driver, javaScriptDriver, capturePageWorker);
        }
    }

    private void capturePage(WebDriver driver,
                             JavaScriptDriver javaScriptDriver,
                             CapturePageWorker capturePageWorker) {

        javaScriptDriver.SetLoadedHtml();

        String lastHtml = driver.getPageSource();
        boolean isReadyToSave = javaScriptDriver.IsReadyToSave();
        capturePageWorker.CapturePage(lastHtml, javaScriptDriver, isReadyToSave);

        int capturesAmount = capturePageWorker.GetAmount();
        if (isReadyToSave) javaScriptDriver.SetSavedHtml(capturesAmount);

        // update count if changed page
        javaScriptDriver.UpdateAmountCaptures(capturesAmount);
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