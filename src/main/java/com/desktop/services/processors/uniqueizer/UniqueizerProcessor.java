package com.desktop.services.processors.uniqueizer;

import com.desktop.services.config.constants.UniqueizerConstants;
import com.desktop.services.processors.interfaces.IDocumentProcess;
import com.desktop.services.utils.CharSwapper;
import com.desktop.services.utils.ScrapperWorker;
import com.desktop.services.utils.UniqueizerWorker;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UniqueizerProcessor implements IDocumentProcess {
    private final String RANDOM_STRING;

    public UniqueizerProcessor() {
        RANDOM_STRING = UniqueizerWorker.GetRandomIntegerString(10);
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(processSwapCharsAsync(document));
        futures.add(processMetaTags(document));
        futures.add(processDataTags(document));
        futures.add(processConnectedFiles(document));
        futures.add(processImgTags(document));
        futures.add(processEmptyDivs(document));
        futures.add(appendEmptyScript(document));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processSwapCharsAsync(Document document) {
        return CompletableFuture.runAsync(() -> document.traverse((node, depth) -> {
            if (node instanceof TextNode textNode) {
                // Transform the text (e.g., to uppercase)
                String transformedText = CharSwapper.ConvertChars(textNode.getWholeText());
                textNode.text(transformedText);
            }
        }));
    }

    private CompletableFuture<Void> processMetaTags(Document document) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Elements metaTags = UniqueizerWorker.ScrapProcessedMeta(document);

        for (Element metaTag : metaTags) {
            futures.add(processTagAttrRandom(metaTag, "content"));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processDataTags(Document document) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Elements tags = UniqueizerWorker.ScrapProcessedTags(document);

        for (Element tag : tags) {
            if (tag.firstChild() instanceof TextNode && !tag.tagName().equals("title")) continue;

            SecureRandom random = new SecureRandom();
            int randomId = random.nextInt(UniqueizerConstants.DATA_ATTRS.size());
            String randomDataAttr = UniqueizerConstants.DATA_ATTRS.get(randomId);

            futures.add(processTagAttrRandom(tag, randomDataAttr, true));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
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
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Elements imgTags = UniqueizerWorker.ScrapImgTags(document);

        for (Element imgTag : imgTags) {
            futures.add(processTagAttrRandom(imgTag, "alt", true));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processEmptyDivs(Document document) {
        return CompletableFuture.runAsync(() -> {
            SecureRandom random = new SecureRandom();
            int amountDiv = random.nextInt(1, 5);
            for (int i = 0; i < amountDiv; i++) {
                appendEmptyDiv(document);
            }
        });
    }

    private CompletableFuture<Void> processTagAttrRandom(Element tag, String attr) {
        return processTagAttrRandom(tag, attr, false);
    }

    private CompletableFuture<Void> processTagAttrRandom(Element tag, String attr, boolean pushAttribute) {
        return CompletableFuture.runAsync(() -> {
            if (pushAttribute) {
                tag.attr(attr, RANDOM_STRING);
                return;
            }

            if (tag.hasAttr(attr))
                tag.attr(attr, RANDOM_STRING);
        });
    }

    private void appendEmptyDiv(Document document) {
        String randomString = UniqueizerWorker.GetRandomCharStringWithPrefix(12);

        Element div = document.createElement("div");
        div.text(randomString);
        div.attr("style", "display: none;");

        document.body().appendChild(div);
    }

    private CompletableFuture<Void> appendEmptyScript(Document document) {
        return CompletableFuture.runAsync(() -> {
            String randomString = UniqueizerWorker.GetRandomCharStringWithPrefix(12);

            Element div = document.createElement("script");
            div.text("console.log('msg: " + randomString + "');");

            document.body().appendChild(div);
        });
    }
}
