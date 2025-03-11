package com.desktop.services.utils;

import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ScrapperWorker {
    public static Elements ScrapStylesheets(Document document) {
        // Вибираємо всі <link> із CSS
        Elements cssLinks = document.select("link[rel*='stylesheet'][href]");
        Elements cssElements = new Elements();

        for (Element cssLink : cssLinks) {
            String href = cssLink.attr("href");
            String cssName = FilenameUtils.getName(href);

            if (cssName.contains(".css")) {
                cssElements.add(cssLink);
            }
        }

        return cssElements;
    }

    public static Elements ScrapAllExternalFiles(Document document) {
        // Вибираємо всі <link> без CSS, src, srcset, poster
        Elements elements = document.select("*[src]:not(script, iframe), " +
                "*[data-lazy-src]:not(script, iframe), " +
                "*[data-lazy-srcset], " +
                "*[srcset], " +
                "*[poster]");

        Elements links = document.select("link[href], meta[content]");
        final List<String> attrs = Arrays.asList("href", "content");

        for (Element link : links) {
            for (String attr : attrs) {
                addAllowedFile(elements, link, attr);
            }
        }

        return elements;
    }

    public static Elements ScrapInlineStylesheets(Document document) {
        // Вибираємо всі <style> із SRC
        return document.select("*[style]");
    }

    public static Elements ScrapBlockStylesheets(Document document) {
        // Вибираємо всі <style> із SRC
        return document.select("style");
    }

    public static Elements ScrapScripts(Document document) {
        // Вибираємо всі <script> із SRC
        return document.select("script[src], script[data-rocket-src]");
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
        for (var el : ContentTypeToSaveAs.entrySet()) {
            if (lowerCaseContentType.startsWith(el.getKey())) return el.getValue();
        }

        return SaveAsEnum.ASSET;
    }

    private static boolean isAbsoluteUrl(String expression) {
        return expression.matches(RegexConstants.IS_ABSOLUTE_URL_REGEX);
    }

    private static void addAllowedFile(Elements elementsTo, Element check, String attr) {
        if (!check.hasAttr(attr)) return;

        String attrValue = check.absUrl(attr);
        if (allowedTypes.contains(FilesWorker.GetFileType(attrValue))) {
            elementsTo.add(check);
        }
    }

    private static int getInvalidFileNameCharIndex(String fileName) {
        for (Character el : INVALID_SPECIFIC_CHARS) {
            int index = fileName.indexOf(el);

            if (index > 0)
                return index;
        }

        return -1;
    }

    public static final Character[] INVALID_SPECIFIC_CHARS = {'"', '*', '<', '>', '?', '|', '#', ':'};
    public static final Set<SaveAsEnum> allowedTypes = new HashSet<>(Arrays.asList(SaveAsEnum.AUDIO, SaveAsEnum.VIDEO, SaveAsEnum.FONT, SaveAsEnum.IMAGES));
    private static final Map<String, SaveAsEnum> ContentTypeToSaveAs = Map.of(
            "text/css", SaveAsEnum.STYLESHEET,
            "text/javascript", SaveAsEnum.SCRIPT,
            "font/", SaveAsEnum.FONT,
            "video/", SaveAsEnum.VIDEO,
            "image/", SaveAsEnum.IMAGES,
            "audio/", SaveAsEnum.AUDIO
    );
}
