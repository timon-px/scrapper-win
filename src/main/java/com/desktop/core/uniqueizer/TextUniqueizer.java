package com.desktop.core.uniqueizer;

import com.desktop.core.common.constants.UniqueizerConstants;
import com.desktop.core.common.dto.UniqueizerRequestDTO;
import com.desktop.core.common.model.UniqueizeHtmlEntry;
import com.desktop.core.common.model.UniqueizeHtmlModel;
import com.desktop.core.utils.PropertiesWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TextUniqueizer {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10)).build();

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private final Document document;
    private final UniqueizerRequestDTO.ProcessingOptions processingOptions;
    private final List<UniqueizeHtmlEntry> documentTextEntries = Collections.synchronizedList(new ArrayList<>());

    private static final boolean SHOULD_PROCESS_TEXT_UNIQUE = true;

    public TextUniqueizer(Document document, UniqueizerRequestDTO.ProcessingOptions processingOptions) {
        this.document = document;
        this.processingOptions = processingOptions;
    }

    public void ProcessNode(TextNode textNode) {
        if (processingOptions.shouldProcessChars()) replaceChars(textNode);

        if (SHOULD_PROCESS_TEXT_UNIQUE)
            registerOnUniqueize(textNode);
    }

    public CompletableFuture<Void> Finish() {
        if (!SHOULD_PROCESS_TEXT_UNIQUE || documentTextEntries.isEmpty())
            return CompletableFuture.completedFuture(null);

        return CompletableFuture
                .supplyAsync(this::buildJson)
                .thenApply(this::buildRequest)
                .thenCompose(this::sendRequest)
                .exceptionally(this::handleError);
    }

    private void replaceChars(TextNode textNode) {
        String transformedText = CharSwapper.ConvertChars(textNode.text());
        textNode.text(transformedText);
    }

    private void registerOnUniqueize(TextNode textNode) {
        if (!textNode.isBlank()) {
            String index = String.valueOf(documentTextEntries.size());

            UniqueizeHtmlEntry newEntry = new UniqueizeHtmlEntry(index, textNode.text());
            documentTextEntries.add(newEntry);

            Element node = document.createElement(UniqueizerConstants.UNIQUEIZE_NODE_NAME);
            node.attr(UniqueizerConstants.UNIQUEIZE_NODE_DATA_ATTR, index);
            textNode.replaceWith(node);
        }
    }

    private CompletableFuture<Void> sendRequest(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse)
                .thenApply(this::buildObject)
                .thenApply(this::validateResponse)
                .thenAccept(this::replaceText);
    }

    private HttpRequest buildRequest(String json) {
        String api = PropertiesWorker.getProperty(UniqueizerConstants.UNIQUEIZER_PROP_API_KEY);
        if (Strings.isNullOrEmpty(api)) throw new IllegalArgumentException("Text uniqueizer API string is empty");

        URI uri = URI.create(api);
        return HttpRequest.newBuilder().uri(uri)
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("ngrok-skip-browser-warning", "any") //remove
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private String buildJson() {
        try {
            UniqueizeHtmlModel requestBody = new UniqueizeHtmlModel(documentTextEntries, "en");
            return jsonMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private UniqueizeHtmlModel buildObject(String json) {
        try {
            return jsonMapper.readValue(json, UniqueizeHtmlModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse response from server");
        }
    }

    private String handleResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
        throw new RuntimeException("Unknown service error: " + response.statusCode());
    }

    private List<UniqueizeHtmlEntry> validateResponse(UniqueizeHtmlModel object) {
        if (object == null) return documentTextEntries;

        List<UniqueizeHtmlEntry> result = object.getHtml();
        if (result == null ||
                result.size() != documentTextEntries.size()) return documentTextEntries;

        return result;
    }

    private void replaceText(List<UniqueizeHtmlEntry> textEntries) {
        for (UniqueizeHtmlEntry textEntry : textEntries) {
            String selector = UniqueizerConstants.UNIQUEIZE_NODE_NAME + getDataAttrSelector(textEntry.getKey());
            Element el = document.select(selector).first();
            if (el == null) continue;

            TextNode textNode = new TextNode(textEntry.getValue());
            el.replaceWith(textNode);
        }
    }

    private Void handleError(Throwable throwable) {
        replaceText(documentTextEntries);
        throw new RuntimeException(throwable.getCause().getMessage());
    }

    private String getDataAttrSelector(String dataId) {
        return String.format("[%s='%s']", UniqueizerConstants.UNIQUEIZE_NODE_DATA_ATTR, dataId);
    }
}
