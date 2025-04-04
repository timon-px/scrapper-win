package com.desktop.services.utils;

import com.desktop.services.config.constants.PathHelperConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilesWorker {
    private static final Tika mimetypeDetector = new Tika();
    private static final Logger log = LoggerFactory.getLogger(FilesWorker.class);

    public static FileSaveModel SetFilesToSave(String absoluteUrl, ConcurrentHashMap<String, FileSaveModel> filesToSave) {
        return filesToSave.computeIfAbsent(absoluteUrl, url -> {
            String fileName = FilenameUtils.getName(URLDecoder.decode(url, StandardCharsets.UTF_8));
            String cleanName = ScrapperWorker.CleanName(fileName);
            String uniqueName = getUniqueName(cleanName, filesToSave.values());
            SaveAsEnum fileType = GetFileType(url);
            return new FileSaveModel(uniqueName, fileType);
        });
    }

    public static SaveAsEnum GetFileType(String absoluteUrl) {
        if (isFontFileType(absoluteUrl)) return SaveAsEnum.FONT;

        return getOtherFileType(absoluteUrl);
    }

    private static boolean isFontFileType(String absoluteUrl) {
        Tika tika = FontDetector.tika;
        if (tika == null) return false;

        String mimetype = tika.detect(absoluteUrl);
        return mimetype.startsWith("font/");
    }

    private static SaveAsEnum getOtherFileType(String absoluteUrl) {
        try {
            String fileName = FilenameUtils.getName(absoluteUrl);
            String mimetype = mimetypeDetector.detect(fileName);

            return ScrapperWorker.GetSaveAsFromContentType(mimetype);
        } catch (Exception e) {
            log.error(e.getMessage());
            return SaveAsEnum.ASSET;
        }
    }

    private static String getUniqueName(String name, Collection<FileSaveModel> names) {
        String uniqueName = URLDecoder.decode(name, StandardCharsets.UTF_8);

        if (!isNameUnique(uniqueName, names)) {
            int counter = 1;

            String extension = FilenameUtils.getExtension(name);
            String nameWithoutExtension = FilenameUtils.getBaseName(name);

            do {
                uniqueName = nameWithoutExtension + "_" + counter + "." + extension;
                counter++;
            } while (!isNameUnique(uniqueName, names));
        }

        return uniqueName;
    }

    private static boolean isNameUnique(String name, Collection<FileSaveModel> names) {
        for (FileSaveModel fileSaveModel : names) {
            if (fileSaveModel.getUniqueName().equals(name)) return false;
        }
        return true;
    }

    private static class FontDetector {
        public static final Tika tika;

        static {
            TikaConfig tikaConfig = null;

            try {
                String configPath = String.format("/%s/%s", PathHelperConstants.TIKA_CONFIG_FOLDER, PathHelperConstants.TIKA_CONFIG_FONT_FILE);
                URL fontConfig = FontDetector.class.getResource(configPath);

                if (fontConfig != null)
                    tikaConfig = new TikaConfig(fontConfig);

            } catch (IOException | SAXException | TikaException e) {
                log.error("Font detector error: {}", e.getMessage());
            }

            if (tikaConfig != null)
                tika = new Tika(tikaConfig);

            else tika = null;
        }
    }
}