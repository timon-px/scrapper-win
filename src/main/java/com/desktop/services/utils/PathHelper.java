package com.desktop.services.utils;

import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PathHelper {
    public static Path GetPath(SaveAsEnum type) {
        return GetPath(type, null);
    }

    public static Path GetPath(SaveAsEnum type, String initFolder) {
        return GetPath(type, initFolder, null);
    }

    public static Path GetPath(SaveAsEnum type, String initFolder, String resolve) {
        if (!MAP.containsKey(type)) return pathCreator(initFolder, ScrapperConstants.MEDIA_FOLDER, resolve);

        String _initFolder = ".";
        if (initFolder != null) _initFolder = initFolder;
        if (resolve == null) resolve = "";

        String folder = MAP.get(type);
        return pathCreator(_initFolder, folder, resolve);
    }

    public static String GetUnixPath(SaveAsEnum type, String initFolder, String resolve) {
        String systemPath = GetPath(type, initFolder, resolve).toString();
        return FilenameUtils.separatorsToUnix(systemPath);
    }

    private static Path pathCreator(String initFolder, String folderPath, String resolve) {
        String normalizedFolderPath = FilenameUtils.normalize(folderPath);
        String normalizedResolve = FilenameUtils.normalize(resolve);
        return Paths.get(initFolder, normalizedFolderPath).resolve(normalizedResolve);
    }

    private static final Map<SaveAsEnum, String> MAP = Map.of(
            SaveAsEnum.ASSET, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.IMAGES, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.STYLESHEET, ScrapperConstants.STYLESHEET_FOLDER,
            SaveAsEnum.FONT, ScrapperConstants.FONT_FOLDER,
            SaveAsEnum.SCRIPT, ScrapperConstants.SCRIPTS_FOLDER,
            SaveAsEnum.VIDEO, ScrapperConstants.VIDEO_FOLDER,
            SaveAsEnum.AUDIO, ScrapperConstants.AUDIO_FOLDER);
}
