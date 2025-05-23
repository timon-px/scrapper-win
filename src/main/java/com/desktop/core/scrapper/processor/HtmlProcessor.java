package com.desktop.core.scrapper.processor;

import com.desktop.core.common.constants.HtmlConstants;
import com.desktop.core.common.constants.RegexConstants;
import com.desktop.core.common.enums.SaveAsEnum;
import com.desktop.core.common.model.FileSaveModel;
import com.desktop.core.scrapper.processor.interfaces.IDocumentProcess;
import com.desktop.core.scrapper.FilesWorker;
import com.desktop.core.scrapper.PathHelper;
import com.desktop.core.scrapper.ScrapperWorker;
import com.desktop.core.utils.*;
import javafx.beans.property.DoubleProperty;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class HtmlProcessor implements IDocumentProcess {
    private final ConcurrentHashMap<String, FileSaveModel> filesToSave;

    public HtmlProcessor(ConcurrentHashMap<String, FileSaveModel> filesToSave) {
        this.filesToSave = filesToSave;
    }

    @Override
    public CompletableFuture<Void> ProcessAsync(Document document, DoubleProperty progress) {
        Elements externalFiles = DocumentWorker.ScrapAllExternalFiles(document);
        Elements scriptFiles = DocumentWorker.ScrapScripts(document);

        String documentUrl = ScrapperWorker.ResolveDocumentUrl(document);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.addAll(processElementsAsync(externalFiles, element -> {
            processSimpleAttr(element);
            processComplexAttr(element, documentUrl);
        }));
        futures.addAll(processElementsAsync(scriptFiles, this::processScriptAttr));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> DocumentWorker.IncrementProgress(progress, 0.1));
    }

    private List<CompletableFuture<Void>> processElementsAsync(Elements elements, Consumer<Element> processor) {
        return elements.stream()
                .map(element -> CompletableFuture.runAsync(() -> processor.accept(element)))
                .collect(Collectors.toList());
    }

    private void processScriptAttr(Element element) {
        processAttribute(element, "src");
        processAttribute(element, "data-rocket-src");
    }

    private void processSimpleAttr(Element element) {
        for (String attr : HtmlConstants.SIMPLE_SAVE_ATTR) { // Move to constants class
            processAttribute(element, attr);
        }
    }

    private void processComplexAttr(Element element, String documentUrl) {
        for (String attr : HtmlConstants.COMPLEX_SAVE_ATTR) {
            if (!element.hasAttr(attr)) continue;

            String attrValue = element.attr(attr);
            String updatedValue = RegexWorker.ProcessStringByRegex(attrValue, RegexConstants.SRCSET_URL_REGEX,
                    url -> resolveAndSaveUrl(url, documentUrl));
            element.attr(attr, updatedValue);
        }
    }

    private void processAttribute(Element element, String attr) {
        if (!element.hasAttr(attr)) return;

        String url = element.attr(attr);
        if (isBase64(url)) return;

        String absoluteUrl = element.absUrl(attr);
        FileSaveModel file = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);

        try {
            String pathToFile = getPathToSave(file.getUniqueName(), file.getFileType());
            element.attr(attr, pathToFile);
        } catch (Exception unused) {
            filesToSave.remove(absoluteUrl);
        }
    }

    private String resolveAndSaveUrl(Matcher matcher, String documentUrl) {
        String url = matcher.group(1);
        if (url == null || url.isEmpty() || isBase64(url)) return null;

        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(documentUrl, url);
        FileSaveModel fileSave = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);

        try {
            String pathToFile = getPathToSave(fileSave.getUniqueName(), fileSave.getFileType());
            return matcher.group().replace(url, pathToFile);
        } catch (Exception unused) {
            filesToSave.remove(absoluteUrl);
            return matcher.group();
        }
    }

    private String getPathToSave(String fileName, SaveAsEnum saveAs) {
        return PathHelper.GetUnixPath(saveAs, null, fileName);
    }

    public static boolean isBase64(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String base64Find = ";base64,";
        // Видаляємо Data URL Scheme, якщо є
        if (input.contains(base64Find)) {
            int indexCutTo = input.indexOf(base64Find) + base64Find.length();
            input = input.substring(indexCutTo);
        }

        return Base64.isBase64(input);
    }
}