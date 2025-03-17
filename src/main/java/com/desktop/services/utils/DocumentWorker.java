package com.desktop.services.utils;

import com.desktop.services.config.constants.ScrapperWorkerConstants;
import com.desktop.services.config.constants.UniqueizerConstants;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;

public class DocumentWorker {
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
        // Вибираємо всі inline styles
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

    public static Elements ScrapProcessedMeta(Document document) {
        // Вибираємо всі <meta> із og:
        return document.select(getProcessMetaSelector());
    }

    public static Elements ScrapProcessedTags(Document document) {
        // Вибираємо всі not excluded tags
        return document.children().select(getProcessTagsSelector());
    }

    public static Elements ScrapImgTags(Document document) {
        // Вибираємо всі images
        return document.select("img");
    }

    public static Elements ScrapTagsWithClasses(Document document) {
        // Вибираємо всі tags with classes
        return document.select("*[class]");
    }

    public static void UpdateProgress(DoubleProperty progress, double addValue) {
        UpdateProgress(progress, addValue, 1.0);
    }

    public static void UpdateProgress(DoubleProperty progress, double addValue, double maxValue) {
        Platform.runLater(() -> progress.set(Math.min(addValue, maxValue)));
    }

    public static double GetProgressIncrement(double maxProgress, double countProcesses) {
        return maxProgress / countProcesses;
    }

    private static void addAllowedFile(Elements elementsTo, Element check, String attr) {
        if (!check.hasAttr(attr)) return;

        String attrValue = check.absUrl(attr);
        if (ScrapperWorkerConstants.ALLOWED_TYPES.contains(FilesWorker.GetFileType(attrValue))) {
            elementsTo.add(check);
        }
    }

    private static String getProcessMetaSelector() {
        int id = 0;
        int length = UniqueizerConstants.OG_PROPERTIES.size();
        StringBuilder builder = new StringBuilder();
        for (String prop : UniqueizerConstants.OG_PROPERTIES) {
            builder.append("meta").append("[").append("property=").append(prop).append("]");

            if (++id < length) builder.append(", ");
        }

        return builder.toString();
    }

    private static String getProcessTagsSelector() {
        String excludedTags = String.join(", ", UniqueizerConstants.EXCLUDED_TAGS);
        return "*" + ":not(" + excludedTags + ")";
    }
}
