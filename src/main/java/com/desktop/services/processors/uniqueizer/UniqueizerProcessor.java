package com.desktop.services.processors.uniqueizer;

import com.desktop.services.processors.interfaces.IDocumentProcess;
import com.desktop.services.utils.CharSwapper;
import com.desktop.services.utils.UniqueizerWorker;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UniqueizerProcessor implements IDocumentProcess {
    private final String RANDOM_STRING;

    public UniqueizerProcessor() {
        RANDOM_STRING = UniqueizerWorker.GetRandomString(10);
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(processSwapCharsAsync(document));
        futures.add(processMetaTags(document));

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
            futures.add(processMetaTag(metaTag));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processMetaTag(Element metaTag) {
        return CompletableFuture.runAsync(() ->
                metaTag.attr("content", RANDOM_STRING)
        );
    }
}
