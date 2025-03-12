package com.desktop.services.services.classes;

import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.dto.UniqueizerRequestDTO;
import com.desktop.services.config.constants.UniqueizerConstants;
import com.desktop.services.services.interfaces.IUniqueizerService;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.storage.StorageWorker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class UniqueizerService implements IUniqueizerService {
    @Override
    public CompletableFuture<ScrapperResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest) {
        IStorageWorker storageWorker = new StorageWorker(uniqueizerRequest.getSavePath());

        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = uniqueizerRequest.getFile();
                Document document = Jsoup.parse(file);

                return startProcesses(document)
                        .thenCompose(unused -> storageWorker.SaveContentAsync(document.outerHtml(), UniqueizerConstants.UNIQUE_HTML_NAME))
                        .thenApply(finalPath -> {
                            Path responsePath = finalPath.getParent().toAbsolutePath();
                            return new ScrapperResponseDTO(true, responsePath.toString());
                        })
                        .exceptionally(ex -> new ScrapperResponseDTO(false, "Error: " + ex.getMessage())).join();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> startProcesses(Document document) {
        return CompletableFuture.runAsync(() -> {
            document.traverse((node, depth) -> {
                if (node instanceof TextNode textNode) {
                    // Transform the text (e.g., to uppercase)
                    System.out.println(textNode.text());
                    String transformedText = textNode.text().toUpperCase();
                    textNode.text(transformedText);
                }
            });
        });
    }
}
