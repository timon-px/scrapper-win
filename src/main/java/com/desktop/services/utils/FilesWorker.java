package com.desktop.services.utils;

import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilesWorker {
    private static final Tika mimetypeDetector = new Tika();

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
        try {
            String fileName = FilenameUtils.getName(absoluteUrl);
            String mimetype = mimetypeDetector.detect(fileName);

            return ScrapperWorker.GetSaveAsFromContentType(mimetype);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
}