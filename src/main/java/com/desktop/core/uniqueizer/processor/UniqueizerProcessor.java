package com.desktop.core.uniqueizer.processor;

import com.desktop.core.common.constants.HtmlConstants;
import com.desktop.core.common.constants.RegexConstants;
import com.desktop.core.common.constants.UniqueizerConstants;
import com.desktop.core.common.dto.UniqueizerRequestDTO;
import com.desktop.core.scrapper.StylesheetWorker;
import com.desktop.core.scrapper.processor.interfaces.IDocumentProcess;
import com.desktop.core.uniqueizer.TextUniqueizer;
import com.desktop.core.uniqueizer.UniqueizerWorker;
import com.desktop.core.utils.DocumentWorker;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.beans.property.DoubleProperty;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueizerProcessor implements IDocumentProcess {
    private static final SecureRandom random = new SecureRandom();
    private final String RANDOM_STRING;
    private final UniqueizerRequestDTO.ProcessingOptions processingOptions;

    public UniqueizerProcessor(UniqueizerRequestDTO.ProcessingOptions processingOptions) {
        this.processingOptions = processingOptions;
        RANDOM_STRING = UniqueizerWorker.GetRandomIntegerString(UniqueizerConstants.RANDOM_STRING_LENGTH);
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document, DoubleProperty progress) {
        // Adding divs and script at the end
        return processNodesAsync(document, progress)
                .thenCompose(unused -> processEmptyDivs(document))
                .thenCompose(unused -> processEmptyScript(document))
                .thenRun(() -> DocumentWorker.UpdateProgress(progress, 1.0));
    }

    private CompletableFuture<Void> processNodesAsync(Document document, DoubleProperty progress) {
        AtomicDouble progressValue = new AtomicDouble(0.0);
        TextUniqueizer textUniqueizer = new TextUniqueizer(document, processingOptions);

        return getNodesAmount(document)
                .thenApply(totalNodes -> UniqueizerConstants.MAX_PROCESS_NODES_PROGRESS / totalNodes)
                .thenAccept(progressStep ->
                        document.traverse((node, depth) -> {
                            if (node instanceof TextNode textNode) {
                                textUniqueizer.ProcessNode(textNode);
                            } else if (node instanceof Element element) {
                                processNodeElement(element);
                            }

                            incrementTraversedProgress(progressValue, progressStep, progress);
                        }))
                .thenCompose(unused -> textUniqueizer.Finish())
                .thenRun(() -> DocumentWorker.UpdateProgress(progress, 0.95));
    }

    private CompletableFuture<Integer> getNodesAmount(Document document) {
        return CompletableFuture.supplyAsync(() -> {
            AtomicInteger totalNodesAtomic = new AtomicInteger(0);
            document.traverse((node, i) -> totalNodesAtomic.incrementAndGet());
            return totalNodesAtomic.get();
        });
    }

    private void processNodeElement(Element element) {
        // Adding or replacement meta tags
        if (element.is(HtmlConstants.UNIQUEIZER_META_TAGS_QUERY))
            replaceRandomAttribute(element, "content");

        // Adding or replacement data tags
        if (element.is(HtmlConstants.UNIQUEIZER_ALL_TAGS_QUERY))
            processDataTag(element);

        // Replacing img alt
        if (element.is(HtmlConstants.HTML_IMAGE_QUERY))
            setRandomAttribute(element, "alt");

        // Adding version (?v=) for stylesheet
        if (element.is(HtmlConstants.LINK_STYLESHEETS_QUERY))
            processConnectedStylesheetFile(element);

        // Adding version (?v=) for script
        if (element.is(HtmlConstants.EXTERNAL_SCRIPTS_QUERY))
            processConnectedFile(element, "src");

        // Changing colors
        if (element.is(HtmlConstants.INLINE_STYLESHEETS_QUERY))
            processInlineStylesheetColorElement(element);

        if (processingOptions.shouldReplaceHref() && element.is(HtmlConstants.ANCHOR_WITH_HREF_QUERY))
            DocumentWorker.SetAttribute(element, "href", "{offer}");

        // Adding or replacement unique classes
        if (element.hasAttr("class"))
            processClass(element);
    }

    private void processDataTag(Element element) {
        removeExistingDataTag(element);

        int randomId = random.nextInt(UniqueizerConstants.DATA_ATTRS.size());
        String randomDataAttr = UniqueizerConstants.DATA_ATTRS.get(randomId);

        setRandomAttribute(element, randomDataAttr);
    }

    private void processConnectedStylesheetFile(Element element) {
        String href = element.attr("href");
        if (!FilenameUtils.getName(href).contains(".css")) return;

        processConnectedFile(element, "href");
    }

    private void processConnectedFile(Element element, String attr) {
        if (element.hasAttr(attr)) {
            String link = element.attr(attr);

            int versionIndex = link.indexOf("?v=");
            if (versionIndex > 0) link = link.substring(0, versionIndex);

            element.attr(attr, link + "?v=" + RANDOM_STRING);
        }
    }

    private void processClass(Element element) {
        cleanUniqueizerClass(element);

        String randomString = UniqueizerWorker.GetRandomCharStringWithPrefix(12);
        element.addClass(randomString);
    }

    private void cleanUniqueizerClass(Element element) {
        Set<String> classList = element.classNames();

        for (String className : classList) {
            if (className.matches(RegexConstants.DATA_REPEAT_CHECK_REGEX))
                element.removeClass(className);
        }
    }

    private void processInlineStylesheetColorElement(Element element) {
        if (!element.hasAttr("style")) return;

        String inlineStyle = element.attr("style");
        Map<String, List<String>> stylesMap;

        try {
            stylesMap = StylesheetWorker.GetStylesMap(inlineStyle);
        } catch (Exception e) {
            return;
        }

        Map<String, List<String>> uniqueStyles = StylesheetWorker.ProcessUniqueStylesColors(stylesMap);
        element.attr("style", StylesheetWorker.GetStylesString(uniqueStyles));
    }

    private CompletableFuture<Void> processEmptyDivs(Document document) {
        return CompletableFuture.runAsync(() -> {
            int amountDiv = random.nextInt(UniqueizerConstants.MIN_EMPTY_DIVS, UniqueizerConstants.MAX_EMPTY_DIVS);
            for (int i = 0; i < amountDiv; i++) {
                appendEmptyDiv(document);
            }
        });
    }

    private void appendEmptyDiv(Document document) {
        String randomString = UniqueizerWorker.GetRandomCharString(UniqueizerConstants.RANDOM_STRING_LENGTH);

        Element div = document.createElement("div");
        div.text(randomString);
        div.attr("style", "display: none;");

        document.body().appendChild(div);
    }

    private CompletableFuture<Void> processEmptyScript(Document document) {
        return CompletableFuture.runAsync(() -> {
            String randomString = UniqueizerWorker.GetRandomIntegerString(UniqueizerConstants.RANDOM_STRING_LENGTH);

            Element div = document.createElement("script");
            div.appendText("console.log('msg: " + randomString + "');");

            document.body().appendChild(div);
        });
    }

    private void setRandomAttribute(Element tag, String attr) {
        DocumentWorker.SetAttribute(tag, attr, RANDOM_STRING);
    }

    private void replaceRandomAttribute(Element tag, String attr) {
        DocumentWorker.ReplaceAttribute(tag, attr, RANDOM_STRING);
    }

    private void removeExistingDataTag(Element element) {
        for (String attr : UniqueizerConstants.DATA_ATTRS) {
            if (element.hasAttr(attr)) element.removeAttr(attr);
        }
    }

    private void incrementTraversedProgress(AtomicDouble progressValue, double progressStep, DoubleProperty progress) {
        double newProgress = progressValue.updateAndGet(pr -> pr + progressStep);
        DocumentWorker.UpdateProgress(progress, newProgress);
    }
}
