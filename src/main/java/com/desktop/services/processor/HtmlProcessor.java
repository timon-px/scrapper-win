package com.desktop.services.processor;

import com.desktop.services.config.constants.HTMLConstants;
import com.desktop.services.config.constants.RegexConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import com.desktop.services.utils.FilesWorker;
import com.desktop.services.utils.PathHelper;
import com.desktop.services.utils.RegexWorker;
import com.desktop.services.utils.ScrapperWorker;
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

public class HtmlProcessor {
    private final ConcurrentHashMap<String, FileSaveModel> filesToSave;

    public HtmlProcessor(ConcurrentHashMap<String, FileSaveModel> filesToSave) {
        this.filesToSave = filesToSave;
    }

    public CompletableFuture<Void> SaveHtmlMediaAsync(Document document) {
        Elements externalFiles = ScrapperWorker.ScrapAllExternalFiles(document);
        Elements scriptFiles = ScrapperWorker.ScrapScripts(document);

        String documentUrl = ScrapperWorker.ResolveDocumentUrl(document);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.addAll(processElementsAsync(externalFiles, element -> {
            processSimpleAttr(element);
            processComplexAttr(element, documentUrl);
        }));
        futures.addAll(processElementsAsync(scriptFiles, this::processScriptAttr));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
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
        for (String attr : HTMLConstants.getSimpleSaveAttr()) { // Move to constants class
            processAttribute(element, attr);
        }
    }

    private void processComplexAttr(Element element, String documentUrl) {
        for (String attr : HTMLConstants.getComplexSaveAttr()) {
            if (!element.hasAttr(attr)) continue;

            String attrValue = element.attr(attr);
            String updatedValue = RegexWorker.ProcessUrlsByRegex(attrValue, RegexConstants.SRCSET_URL_REGEX,
                    url -> resolveAndSaveUrl(url, documentUrl));
            element.attr(attr, updatedValue);
        }
    }

    private void processAttribute(Element element, String attr) {
        if (!element.hasAttr(attr)) return;
        String absoluteUrl = element.absUrl(attr);

        FileSaveModel file = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);
        element.attr(attr, getPathToSave(file.getUniqueName(), file.getFileType()));
    }

    private String resolveAndSaveUrl(Matcher matcher, String documentUrl) {
        String url = matcher.group(1);
        if (url == null || url.isEmpty()) return null;

        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(documentUrl, url);
        FileSaveModel fileSave = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);

        return matcher.group()
                .replace(url, getPathToSave(fileSave.getUniqueName(), fileSave.getFileType()));
    }

    private String getPathToSave(String fileName, SaveAsEnum saveAs) {
        return PathHelper.GetUnixPath(saveAs, null, fileName);
    }
}