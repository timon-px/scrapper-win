package com.desktop.application.validation;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UniqueizerValidation {
    public static String validateFilePathField(String path) {
        if (path == null || path.isBlank()) {
            return "Path field is empty! Please enter a valid path";
        }
        Path dir = Paths.get(path);
        File file = dir.toFile();

        if (!Files.exists(dir) || !file.isFile()) {
            return "File doesn't exist! Please choose an existing file";
        }

        Tika tika = new Tika();
        try {
            String mimeType = tika.detect(file);
            if (!mimeType.equals("text/html")) return "File type is not HTML: " + file.getName();
        } catch (IOException e) {
            return "Error detecting the file";
        }

        return null;
    }

    public static String validateSavePathField(String path) {
        if (path == null || path.isBlank()) {
            return "Path field is empty! Please enter a valid path";
        }
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return "Directory doesn't exist! Please choose a directory or create a new one";
        }

        return null;
    }
}
