package com.desktop.services.utils;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.constants.ScrapperWorkerConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.driver.ScrapperDriver;
import com.desktop.services.models.DriverSaveModel;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ScrapperWorker {
    private static final Logger log = LoggerFactory.getLogger(ScrapperWorker.class);

    public static Document GetDocument(String url) throws IOException {
        return getJsoupResponse(url).parse();
    }

    public static Document GetDocument(String url, ScrapperRequestDTO.ProcessingOptions processingOptions) throws IOException {
        if (processingOptions == null || !processingOptions.shouldProcessDriver()) {
            return GetDocument(url);
        }

        ScrapperDriver scrapperDriver = new ScrapperDriver(url);
        boolean shouldProcessStyles = processingOptions.shouldProcessDriverCustomStyles();

        String baseUri = getJsoupResponse(url).parse().baseUri();
        List<DriverSaveModel> driverSaveModels = scrapperDriver
                .RunWebDriver(shouldProcessStyles)
                .join();

        if (driverSaveModels.isEmpty())
            throw new RuntimeException("Scrapper hasn't captured any HTML.\nPlease try again");

        DriverSaveModel driverSaveModel = driverSaveModels.getLast();
        return getDriverDocument(driverSaveModel, baseUri, shouldProcessStyles);
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

    private static Document getDriverDocument(DriverSaveModel driverSaveModel,
                                              String baseUri,
                                              boolean shouldProcessDriverCustomStyles) {

        Document document = Jsoup.parse(driverSaveModel.getHtml(), baseUri);
        if (shouldProcessDriverCustomStyles) return document;

        return setCutomStyleDocument(document, driverSaveModel);
    }

    private static Document setCutomStyleDocument(Document document, DriverSaveModel driverSaveModel) {
        if (driverSaveModel.getStyle() == null) return document;
        document.select("style").remove();
        document.select(".hide-when-no-script").remove();
        return DocumentWorker.AppendStyleBlock(document, driverSaveModel.getStyle());
    }
}
