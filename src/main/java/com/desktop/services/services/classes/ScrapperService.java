package com.desktop.services.services.classes;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.services.models.FileSaveModel;
import com.desktop.services.processor.FilesProcessor;
import com.desktop.services.processor.HtmlProcessor;
import com.desktop.services.processor.StylesheetProcessor;
import com.desktop.services.services.interfaces.IScrapperService;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.storage.StorageWorker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ScrapperService implements IScrapperService {
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    public CompletableFuture<ScrapperResponseDTO> GetWeb(ScrapperRequestDTO scrapperRequest) {
        IStorageWorker storageWorker = new StorageWorker(scrapperRequest.getDirectory());

        progress.set(0.0);
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<String, FileSaveModel> filesToSaveList = new ConcurrentHashMap<>();

            try {
                Connection.Response response = Jsoup.connect(scrapperRequest.getUrl()).execute();
                Document document = response.parse();
                progress.set(progress.get() + 0.05);

                String fileHost = new URI(document.location()).getHost();
                Path path = storageWorker.GetFolderPath(fileHost);

                return startProcesses(document, path, filesToSaveList, storageWorker)
                        .thenCompose(unused -> {
                            document.select("base").remove();
                            replaceHrefToOffer(document, scrapperRequest.isReplaceSelected());
                            return storageWorker.SaveContentAsync(document.outerHtml(), path, ScrapperConstants.HTML_NAME);
                        })
                        .thenApply(finalPath -> {
                            progress.set(1.0);
                            return new ScrapperResponseDTO(true, path.toAbsolutePath().toString());
                        })
                        .exceptionally(ex -> new ScrapperResponseDTO(false, "Error: " + ex.getMessage())).join();
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> startProcesses(Document document, Path path, ConcurrentHashMap<String, FileSaveModel> filesToSaveList, IStorageWorker storageWorker) {
        StylesheetProcessor stylesheetProcessor = new StylesheetProcessor(storageWorker, path, filesToSaveList);
        FilesProcessor filesProcessor = new FilesProcessor(storageWorker, path);
        HtmlProcessor htmlProcessor = new HtmlProcessor(filesToSaveList);

        return stylesheetProcessor.SaveStylesheetsAsync(document)
                .thenRun(() -> progress.set(progress.get() + 0.05))
                .thenCompose(unused -> htmlProcessor.SaveHtmlMediaAsync(document))
                .thenRun(() -> progress.set(progress.get() + 0.05))
                .thenCompose(unused -> filesProcessor.SaveFilesAsync(filesToSaveList, progress));
    }

    private void replaceHrefToOffer(Document document, boolean isNeeded) {
        if (!isNeeded) return;
        Elements links = document.select("a[href]");
        links.attr("href", "{offer}");
    }
}
