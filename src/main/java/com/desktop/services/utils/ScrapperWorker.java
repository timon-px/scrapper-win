package com.desktop.services.utils;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.constants.ScrapperWorkerConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.google.common.base.Strings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ScrapperWorker {
    private static final Logger log = LoggerFactory.getLogger(ScrapperWorker.class);

    public static Document GetDocument(String url) throws IOException {
        return getJsoupResponse(url).parse();
    }

    public static Document GetDocument(String url, ScrapperRequestDTO.ProcessingOptions processingOptions) throws IOException {
        if (processingOptions == null || !processingOptions.shouldProcessDriver()) {
            return GetDocument(url);
        }

        String baseUri = getJsoupResponse(url).parse().baseUri();
        String html = runWebDriver(url).join();

        if (Strings.isNullOrEmpty(html))
            throw new RuntimeException("Please try again");

        return Jsoup.parse(html, baseUri);
    }

    public static String CleanName(String fileName) {
        int invalidIndex = getInvalidFileNameCharIndex(fileName);
        if (invalidIndex > 0) return fileName.substring(0, invalidIndex);

        return fileName;
    }

    public static String ResolveDocumentUrl(Document document) {
        try {
            return URI.create(document.baseUri()).toString();
        } catch (Exception e) {
            return document.location();
        }
    }

    public static String ResolveAbsoluteUrl(String baseUrl, String relativeUrl) {
        if (isAbsoluteUrl(relativeUrl)) return relativeUrl;

        try {
            URI baseUri = new URI(baseUrl);
            URI resolvedUri = baseUri.resolve(relativeUrl);
            return resolvedUri.toString();
        } catch (URISyntaxException e) {
            return relativeUrl;
        }
    }

    public static SaveAsEnum GetSaveAsFromContentType(String contentType) {
        String lowerCaseContentType = contentType.toLowerCase();
        for (var el : ScrapperWorkerConstants.CONTENT_TYPE_TO_SAVE_AS.entrySet()) {
            if (lowerCaseContentType.startsWith(el.getKey())) return el.getValue();
        }

        return SaveAsEnum.ASSET;
    }

    private static boolean isAbsoluteUrl(String expression) {
        return expression.matches(RegexConstants.IS_ABSOLUTE_URL_REGEX);
    }

    private static int getInvalidFileNameCharIndex(String fileName) {
        for (Character el : ScrapperWorkerConstants.INVALID_SPECIFIC_CHARS) {
            int index = fileName.indexOf(el);

            if (index > 0)
                return index;
        }

        return -1;
    }

    private static Connection getJsoupConnection(String url) {
        return Jsoup.connect(url);
    }

    private static Connection.Response getJsoupResponse(String url) throws IOException {
        return getJsoupConnection(url).execute();
    }

    private static Connection.Response getJsoupResponse(String url, Proxy proxy) throws IOException {
        return getJsoupConnection(url).proxy(proxy).execute();
    }

    private static CompletableFuture<String> runWebDriver(String url) {
        return CompletableFuture.supplyAsync(() -> {
                    WebDriver driver = new ChromeDriver();

                    driver.get(url);
                    driver.manage().window().maximize();

                    return driver;
                }).thenCompose(ScrapperWorker::waitForDriverToClose)
                .exceptionally(throwable -> {
                    throw new RuntimeException("Error during initial web driver");
                });
    }

    private static CompletableFuture<String> waitForDriverToClose(WebDriver driver) {
        return CompletableFuture.supplyAsync(() -> {
            String html = null;

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                wait.until(webDriver -> webDriver.getTitle() != null && !webDriver.getTitle().isEmpty());

                while (true) {
                    try {
                        html = driver.getPageSource();
                        Thread.sleep(Duration.ofMillis(500));
                    } catch (Exception unused) {
                        break;
                    }
                }
            } catch (Exception ex) {
                log.error("Error while processing web driver: {}", ex.getMessage());
            } finally {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.error("Error quitting driver, maybe already closed", e);
                }
            }

            return Strings.nullToEmpty(html);
        });
    }
}
