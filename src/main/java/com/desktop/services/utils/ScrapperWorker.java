package com.desktop.services.utils;

import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.constants.ScrapperWorkerConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

public class ScrapperWorker {
    public static Connection.Response GetJsoupResponse(String url) throws IOException {
        return getJsoupConnection(url).execute();
    }

    public static Connection.Response GetJsoupResponse(String url, Proxy proxy) throws IOException {
        return getJsoupConnection(url).proxy(proxy).execute();
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
            System.out.println("Failed to resolve absolute url: " + relativeUrl);
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
}
