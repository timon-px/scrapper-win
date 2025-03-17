package com.desktop.application.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScrapperValidation {
    public static String validatePathField(String path) {
        if (path == null || path.isBlank()) {
            return "Path field is empty! Please enter a valid path";
        }
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return "Directory doesn't exist! Please choose a directory or create a new one";
        }

        return null;
    }

    public static String validateUrlField(String url) {
        if (url == null || url.isBlank()) {
            return "Url field is empty! Please enter a valid url";
        }

        return null;
    }
}
