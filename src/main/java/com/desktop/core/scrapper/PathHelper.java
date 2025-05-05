package com.desktop.core.scrapper;

import com.desktop.core.common.constants.PathHelperConstants;
import com.desktop.core.common.constants.ScrapperConstants;
import com.desktop.core.common.enums.SaveAsEnum;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathHelper {
    public static Path GetPath(SaveAsEnum type) {
        return GetPath(type, null);
    }

    public static Path GetPath(SaveAsEnum type, String initFolder) {
        return GetPath(type, initFolder, null);
    }

    public static Path GetPath(SaveAsEnum type, String initFolder, String resolve) {
        if (!PathHelperConstants.MAP.containsKey(type))
            return pathCreator(initFolder, ScrapperConstants.MEDIA_FOLDER, resolve);

        String _initFolder = ".";
        if (initFolder != null) _initFolder = initFolder;
        if (resolve == null) resolve = "";

        String folder = PathHelperConstants.MAP.get(type);
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
}
