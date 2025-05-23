package com.desktop.core.utils;

import com.desktop.core.common.constants.HtmlConstants;
import com.desktop.core.common.constants.ScrapperWorkerConstants;
import com.desktop.core.scrapper.FilesWorker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

    public static Elements ScrapFilteredTags(Document document) {
        // Вибираємо всі фільтровані теги
        return document.select(HtmlConstants.FILTER_TAGS_QUERY);
    }

    public static Elements ScrapFilteredSrc(Document document) {
        // Вибираємо всі фільтровані src keywords
        Elements resultElements = new Elements();
        Elements srcElements = document.select("script[src], iframe[src]");

        for (Element src : srcElements) {
            if (!src.hasAttr("src")) continue;

            for (String keyword : HtmlConstants.FILTER_SRC_KEYWORDS) {
                if (!src.attr("src").contains(keyword)) continue;
                resultElements.add(src);
                break;
            }
        }

        return resultElements;
    }

    public static Elements ScrapFilteredInlineScript(Document document) {
        // Вибираємо всі фільтровані <script> keywords
        Elements resultElements = new Elements();
        Elements scriptElements = document.select("script:not([src])");

        for (Element script : scriptElements) {
            String html = script.html().toLowerCase();
            for (String keyword : HtmlConstants.FILTER_SCRIPT_KEYWORDS) {
                if (!html.contains(keyword)) continue;
                resultElements.add(script);
                break;
            }
        }

        return resultElements;
    }

    public static Document AppendStyleBlock(Document document, String css) {
        Element styleNode = document.createElement("style");
        styleNode.append(css);
        document.head().appendChild(styleNode);
        return document;
    }

    public static void SetAttribute(Element element, String attr, String value) {
        element.attr(attr, value);
    }

    public static void ReplaceAttribute(Element element, String attr, String value) {
        if (element.hasAttr(attr)) SetAttribute(element, attr, value);
    }

    public static void ReplaceAnchorHref(Document document, String replaceWith) {
        Elements links = document.select(HtmlConstants.ANCHOR_WITH_HREF_QUERY);
        links.attr("href", replaceWith);
        links.removeAttr("target");
    }

    public static void IncrementProgress(DoubleProperty progress, double addValue) {
        UpdateProgress(progress, progress.get() + addValue, 1.0);
    }

    public static void UpdateProgress(DoubleProperty progress, double newValue) {
        UpdateProgress(progress, newValue, 1.0);
    }

    public static void UpdateProgress(DoubleProperty progress, double newValue, double maxValue) {
        Platform.runLater(() -> progress.set(Math.min(newValue, maxValue)));
    }

    public static double GetProgressIncrement(double completed, double overall) {
        return completed / overall;
    }

    public static void BindOverallProgress(DoubleProperty overallProgress, List<SimpleDoubleProperty> fileProgresses) {
        bindProgressListener(overallProgress, overallProgress, fileProgresses);

        // Bind each file's progress to trigger the overall update
        for (DoubleProperty fileProgress : fileProgresses) {
            bindProgressListener(fileProgress, overallProgress, fileProgresses);
        }
    }

    private static void bindProgressListener(DoubleProperty currentProgress, DoubleProperty overallProgress, List<SimpleDoubleProperty> fileProgresses) {
        currentProgress.addListener((obs, oldVal, newVal) -> {
            double total = fileProgresses.stream()
                    .mapToDouble(DoubleProperty::get)
                    .sum();
            double average = fileProgresses.isEmpty() ? 0.0 : total / fileProgresses.size();
            overallProgress.set(average);
        });
    }

    private static void addAllowedFile(Elements elementsTo, Element check, String attr) {
        if (!check.hasAttr(attr)) return;

        String attrValue = check.absUrl(attr);
        if (ScrapperWorkerConstants.ALLOWED_TYPES.contains(FilesWorker.GetFileType(attrValue))) {
            elementsTo.add(check);
        }
    }
}
