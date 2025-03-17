package com.desktop.services.processors.uniqueizer;

import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.constants.UniqueizerConstants;
import com.desktop.services.processors.interfaces.IDocumentProcess;
import com.desktop.services.utils.CharSwapper;
import com.desktop.services.utils.ScrapperWorker;
import com.desktop.services.utils.StylesheetWorker;
import com.desktop.services.utils.UniqueizerWorker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UniqueizerProcessor implements IDocumentProcess {
    private static final SecureRandom random = new SecureRandom();
    private final String RANDOM_STRING;

    public UniqueizerProcessor() {
        RANDOM_STRING = UniqueizerWorker.GetRandomIntegerString(UniqueizerConstants.RANDOM_STRING_LENGTH);
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document) {
        return ProcessAsync(document, null);
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document, DoubleProperty progress) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Text replacement
        futures.add(processSwapCharsAsync(document));
        // Adding or replacement meta tags
        futures.add(processMetaTags(document));
        // Adding random data tag
        futures.add(processDataTags(document));
        // Adding version (?v=) for connected files
        futures.add(processConnectedFiles(document));
        // Replacing img alt
        futures.add(processImgTags(document));
        // Adding or replacement unique classes
        futures.add(processClasses(document));
        // Changing colors
        futures.add(processInlineStylesheetColors(document));

        double increment = 1.0 / (futures.size() + 2); // +2 for divs and script
        for (CompletableFuture<Void> future : futures) {
            future.thenRun(() -> updateProgress(progress, increment));
        }

        // Adding divs and script at the end
        CompletableFuture<Void> finalFuture = processEmptyDivs(document)
                .thenRun(() -> updateProgress(progress, increment))
                .thenCompose(unused -> processEmptyScript(document))
                .thenRun(() -> updateProgress(progress, increment));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenCompose(unused -> finalFuture)
                .thenRun(() -> progress.set(1));
    }

    private CompletableFuture<Void> processSwapCharsAsync(Document document) {
        return CompletableFuture.runAsync(() -> document.traverse((node, depth) -> {
            if (node instanceof TextNode textNode) {
                String transformedText = CharSwapper.ConvertChars(textNode.getWholeText());
                textNode.text(transformedText);
            }
        }));
    }

    private CompletableFuture<Void> processMetaTags(Document document) {
        Elements metaTags = UniqueizerWorker.ScrapProcessedMeta(document);
        return processElementsAsync(metaTags, element -> replaceRandomAttribute(element, "content"));
    }

    private CompletableFuture<Void> processDataTags(Document document) {
        Elements tags = UniqueizerWorker.ScrapProcessedTags(document);
        return processElementsAsync(tags, element -> {
            if (element.firstChild() instanceof TextNode && !element.tagName().equals("title")) return;

            int randomId = random.nextInt(UniqueizerConstants.DATA_ATTRS.size());
            String randomDataAttr = UniqueizerConstants.DATA_ATTRS.get(randomId);

            setRandomAttribute(element, randomDataAttr);
        });
    }

    private CompletableFuture<Void> processConnectedFiles(Document document) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Elements stylesheets = ScrapperWorker.ScrapStylesheets(document);
        Elements scripts = ScrapperWorker.ScrapScripts(document);

        for (Element stylesheet : stylesheets)
            futures.add(processConnectedFile(stylesheet, "href"));

        for (Element script : scripts)
            futures.add(processConnectedFile(script, "src"));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processConnectedFile(Element stylesheet, String attr) {
        return CompletableFuture.runAsync(() -> {
            if (stylesheet.hasAttr(attr)) {
                String link = stylesheet.attr(attr);

                int versionIndex = link.indexOf("?v=");
                if (versionIndex > 0) link = link.substring(0, versionIndex);

                stylesheet.attr(attr, link + "?v=" + RANDOM_STRING);
            }
        });
    }

    private CompletableFuture<Void> processImgTags(Document document) {
        Elements imgTags = UniqueizerWorker.ScrapImgTags(document);
        return processElementsAsync(imgTags, element -> setRandomAttribute(element, "alt"));
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
            div.text("console.log('msg: " + randomString + "');");

            document.body().appendChild(div);
        });
    }

    private CompletableFuture<Void> processClasses(Document document) {
        Elements elements = UniqueizerWorker.ScrapTagsWithClasses(document);
        return processElementsAsync(elements, this::processClass);
    }

    private void processClass(Element element) {
        cleanUniqueizerClass(element)
                .thenRun(() -> {
                    String randomString = UniqueizerWorker.GetRandomCharStringWithPrefix(12);
                    element.addClass(randomString);
                });
    }

    private CompletableFuture<Void> cleanUniqueizerClass(Element element) {
        return CompletableFuture.runAsync(() -> {
            Set<String> classList = element.classNames();

            for (String className : classList) {
                if (className.matches(RegexConstants.DATA_REPEAT_CHECK_REGEX))
                    element.removeClass(className);
            }
        });
    }

    private CompletableFuture<Void> processInlineStylesheetColors(Document document) {
        Elements stylesheets = ScrapperWorker.ScrapInlineStylesheets(document);
        return processElementsAsync(stylesheets, this::processInlineStylesheetColorElement);
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

    private CompletableFuture<Void> processElementsAsync(Elements elements,
                                                         Consumer<Element> processor) {
        List<CompletableFuture<Void>> futures = elements.stream()
                .map(element -> CompletableFuture.runAsync(() -> processor.accept(element)))
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void setRandomAttribute(Element tag, String attr) {
        tag.attr(attr, RANDOM_STRING);
    }

    private void replaceRandomAttribute(Element tag, String attr) {
        if (tag.hasAttr(attr)) {
            tag.attr(attr, RANDOM_STRING);
        }
    }

    private void updateProgress(DoubleProperty progress, double addValue) {
        Platform.runLater(() -> progress.set(Math.min(addValue, 1.0)));
    }
}
