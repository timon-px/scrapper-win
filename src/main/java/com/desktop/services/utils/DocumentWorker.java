package com.desktop.services.utils;

import com.desktop.services.config.constants.HtmlConstants;
import com.desktop.services.config.constants.ScrapperWorkerConstants;
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
        Elements cssLinks = document.select(HtmlConstants.LINK_STYLESHEETS_QUERY);
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
        Elements elements = document.select(HtmlConstants.EXTERNAL_FILES_QUERY);

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
        return document.select(HtmlConstants.INLINE_STYLESHEETS_QUERY);
    }

    public static Elements ScrapBlockStylesheets(Document document) {
        // Вибираємо всі <style> із SRC
        return document.select(HtmlConstants.BLOCK_STYLESHEETS_QUERY);
    }

    public static Elements ScrapScripts(Document document) {
        // Вибираємо всі <script> із SRC
        return document.select(HtmlConstants.EXTERNAL_SCRIPTS_QUERY);
    }

    public static Elements ScrapProcessedMeta(Document document) {
        // Вибираємо всі <meta> із og:
        return document.select(HtmlConstants.UNIQUEIZER_META_TAGS_QUERY);
    }

    public static Elements ScrapProcessedTags(Document document) {
        // Вибираємо всі not excluded tags
        return document.children().select(HtmlConstants.UNIQUEIZER_ALL_TAGS_QUERY);
    }

    public static Elements ScrapImgTags(Document document) {
        // Вибираємо всі images
        return document.select(HtmlConstants.HTML_IMAGE_QUERY);
    }

    public static Elements ScrapTagsWithClasses(Document document) {
        // Вибираємо всі tags with classes
        return document.select(HtmlConstants.NODE_WITH_CLASS_QUERY);
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
}
