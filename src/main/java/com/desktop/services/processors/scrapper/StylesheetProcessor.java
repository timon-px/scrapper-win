package com.desktop.services.processors.scrapper;

import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import com.desktop.services.processors.interfaces.IScrapperProcess;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.utils.FilesWorker;
import com.desktop.services.utils.PathHelper;
import com.desktop.services.utils.ScrapperWorker;
import com.desktop.services.utils.StylesheetWorker;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StylesheetProcessor implements IScrapperProcess {
    private final IStorageWorker storageWorker;
    private final Path stylesheetPath;
    private final ConcurrentHashMap<String, FileSaveModel> filesToSave;
    private final ConcurrentHashMap<String, String> usedFileNames = new ConcurrentHashMap<>();
    private final CssPathResolver pathResolver;

    public StylesheetProcessor(IStorageWorker storageWorker, Path mainPath, ConcurrentHashMap<String, FileSaveModel> filesToSave) {
        this.storageWorker = storageWorker;
        this.stylesheetPath = mainPath.resolve(ScrapperConstants.STYLESHEET_FOLDER);
        this.filesToSave = filesToSave;
        this.pathResolver = new CssPathResolver();
    }


    @Override
    public CompletableFuture<Void> ProcessAsync(Document document) {
        String documentUrl = ScrapperWorker.ResolveDocumentUrl(document);

        Elements styles = ScrapperWorker.ScrapStylesheets(document);
        Elements blockStyles = ScrapperWorker.ScrapBlockStylesheets(document);
        Elements inlineStyles = ScrapperWorker.ScrapInlineStylesheets(document);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(processBlockStylesAsync(blockStyles, documentUrl));
        futures.add(processInlineStylesAsync(inlineStyles, documentUrl));
        futures.add(processExternalStylesAsync(styles));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processBlockStylesAsync(Elements styles, String documentUrl) {
        return CompletableFuture.runAsync(() -> {
            for (Element style : styles) {
                String cssContent = style.html();
                String updatedCssContent = processCssDependencies(cssContent, documentUrl, true);
                style.html(updatedCssContent);
            }
        });
    }

    private CompletableFuture<Void> processInlineStylesAsync(Elements styles, String documentUrl) {
        return CompletableFuture.runAsync(() -> {
            for (Element style : styles) {
                if (!style.hasAttr("style")) continue;
                String cssContent = style.attr("style");

                String updatedCssContent = processCssDependencies(cssContent, documentUrl, true);
                style.attr("style", updatedCssContent);
            }
        });
    }

    private CompletableFuture<Void> processExternalStylesAsync(Elements styles) {
        return CompletableFuture.runAsync(() -> {
            for (Element style : styles) {
                String href = style.absUrl("href");
                String cssName = processStylesheetFile(href);

                if (cssName != null) {
                    style.attr("href", pathResolver.resolveExternalClear(cssName, SaveAsEnum.STYLESHEET, "."));
                }
            }
        });
    }

    private String processStylesheetFile(String cssUrl) {
        if (usedFileNames.containsKey(cssUrl)) return usedFileNames.get(cssUrl);

        String cssName = FilenameUtils.getName(cssUrl);
        String cleanCssName = ScrapperWorker.CleanName(cssName);
        String uniqueCssName = generateUniqueFileName(cssUrl, cleanCssName);

        try {
            String cssContent = fetchCssContent(cssUrl);
            String updatedCss = processCssDependencies(cssContent, cssUrl, false);

            storageWorker.SaveContentAsync(updatedCss, stylesheetPath, uniqueCssName).join();
            return uniqueCssName;
        } catch (IOException e) {
            System.out.println("PROCESS CSS FILE ERROR " + e.getMessage());
            return null;
        }

    }

    private String processCssDependencies(String cssContent, String baseUrl, boolean isInline) {
        return StylesheetWorker.ProcessDependencies(cssContent,
                importUrl -> processImportUrl(importUrl, baseUrl, isInline),
                externalUrl -> processExternalUrl(externalUrl, baseUrl, isInline));
    }

    private String processImportUrl(String importUrl, String baseUrl, boolean isInline) {
        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(baseUrl, importUrl);
        SaveAsEnum fileType = FilesWorker.GetFileType(absoluteUrl);

        boolean isInitiallyAbsolute = absoluteUrl.equals(importUrl);
        if (fileType != SaveAsEnum.STYLESHEET && isInitiallyAbsolute)
            return pathResolver.resolveInitiallyAbsolute(importUrl);

        if (fileType != SaveAsEnum.STYLESHEET)
            return processExternalUrl(importUrl, baseUrl, isInline);


        String cssName = processStylesheetFile(absoluteUrl);
        return cssName != null ? pathResolver.resolveImport(cssName) : null;
    }

    private String processExternalUrl(String url, String baseUrl, boolean isInline) {
        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(baseUrl, url);

        if (usedFileNames.containsKey(absoluteUrl)) {
            return pathResolver.resolve(usedFileNames.get(absoluteUrl), FilesWorker.GetFileType(absoluteUrl), isInline);
        }

        FileSaveModel file = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);
        return pathResolver.resolve(file.getUniqueName(), file.getFileType(), isInline);
    }

    private String fetchCssContent(String cssUrl) throws IOException {
        Connection.Response response = Jsoup.connect(cssUrl).ignoreContentType(true).execute();
        return response.body();
    }

    private String generateUniqueFileName(String url, String name) {
        String uniqueName = URLDecoder.decode(name, StandardCharsets.UTF_8);
        String extension = FilenameUtils.getExtension(name);
        String nameWithoutExtension = FilenameUtils.getBaseName(name);

        int counter = 1;
        while (usedFileNames.contains(uniqueName)) {
            uniqueName = nameWithoutExtension + "_" + counter + "." + extension;
            counter++;
        }

        usedFileNames.put(url, uniqueName);
        return uniqueName;
    }

    // Inner class to handle path resolution
    private static class CssPathResolver {
        private static final String URL_PATTERN = "url('%s')";
        private static final String IMPORT_PATTERN = "@import url('%s')";
        private static final String IMPORT_PATTERN_INITIALLY_ABSOLUTE = "@import '%s'";

        public String resolve(String fileName, SaveAsEnum fileType, boolean isInline) {
            if (fileType == SaveAsEnum.STYLESHEET) {
                return resolveImport(fileName);
            }
            return resolveExternal(fileName, fileType, isInline ? "." : "..");
        }

        private String resolveExternal(String fileName, SaveAsEnum fileType, String baseFolder) {
            return String.format(URL_PATTERN, resolveExternalClear(fileName, fileType, baseFolder));
        }

        private String resolveInitiallyAbsolute(String url) {
            return String.format(IMPORT_PATTERN_INITIALLY_ABSOLUTE, url);
        }

        public String resolveImport(String fileName) {
            return String.format(IMPORT_PATTERN, "./" + fileName);
        }

        private String resolveExternalClear(String fileName, SaveAsEnum fileType, String baseFolder) {
            return PathHelper.GetUnixPath(fileType, baseFolder, fileName);
        }
    }
}

