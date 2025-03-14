package com.desktop.services.services.classes;

import com.desktop.dto.UniqueizerRequestDTO;
import com.desktop.dto.UniqueizerResponseDTO;
import com.desktop.services.config.constants.UniqueizerConstants;
import com.desktop.services.processors.interfaces.IDocumentProcess;
import com.desktop.services.processors.uniqueizer.UniqueizerProcessor;
import com.desktop.services.services.interfaces.IUniqueizerService;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.storage.StorageWorker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class UniqueizerService implements IUniqueizerService {
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    public CompletableFuture<UniqueizerResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest) {
        IStorageWorker storageWorker = new StorageWorker(uniqueizerRequest.getSavePath());

        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = uniqueizerRequest.getFile();
                Document document = Jsoup.parse(file);

                return startProcesses(document)
                        .thenCompose(unused -> storageWorker.SaveContentAsync(document.outerHtml(), UniqueizerConstants.UNIQUE_HTML_NAME))
                        .thenApply(finalPath -> {
                            Path responsePath = finalPath.getParent().toAbsolutePath();
                            return new UniqueizerResponseDTO(true, responsePath.toString());
                        })
                        .exceptionally(ex -> new UniqueizerResponseDTO(false, "Error: " + ex.getMessage())).join();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> startProcesses(Document document) {
        IDocumentProcess uniqueizerProcessor = new UniqueizerProcessor();
        return uniqueizerProcessor.ProcessAsync(document, progress);
    }
}
