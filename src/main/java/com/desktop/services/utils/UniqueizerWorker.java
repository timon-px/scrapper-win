package com.desktop.services.utils;

import com.desktop.services.config.constants.UniqueizerConstants;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;

public class UniqueizerWorker {
    private static final SecureRandom random = new SecureRandom();

    public static String GetRandomIntegerString(int length) {
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(10);
            randomString.append(number);
        }
        return randomString.toString();
    }

    public static String GetRandomCharString(int length) {
        StringBuilder randomString = new StringBuilder();
        int randomBound = UniqueizerConstants.ALLOWED_TO_RANDOM_STRING.length();
        for (int i = 0; i < length; i++) {
            int randomId = random.nextInt(randomBound);
            char randomChar = UniqueizerConstants.ALLOWED_TO_RANDOM_STRING.charAt(randomId);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    public static String GetRandomCharStringWithPrefix(int length) {
        return GetRandomPrefix() + GetRandomCharString(length);
    }

    public static String GetRandomPrefix() {
        int randomId = random.nextInt(UniqueizerConstants.PREFIXES.size());

        return UniqueizerConstants.PREFIXES.get(randomId);
    }

    public static String GetUniqueizerFileName(File file) {
        String fileName = file.getName();
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);

        return baseName + UniqueizerConstants.UNIQUE_SUFFIX_NAME + "." + extension;
    }

    public static void BindOverallProgress(DoubleProperty overallProgress, List<SimpleDoubleProperty> fileProgresses) {
        bindProgressListener(overallProgress, overallProgress, fileProgresses);

        // Bind each file's progress to trigger the overall update
        for (DoubleProperty fileProgress : fileProgresses) {
            bindProgressListener(fileProgress, overallProgress, fileProgresses);
        }
    }

    private static void bindProgressListener(DoubleProperty currentProgress, DoubleProperty overallProgress, List<SimpleDoubleProperty> fileProgresses) {
        currentProgress.addListener((obs, oldVal, newVal) -> {
            double total = fileProgresses.stream()
                    .mapToDouble(DoubleProperty::get)
                    .sum();
            double average = fileProgresses.isEmpty() ? 0.0 : total / fileProgresses.size();
            overallProgress.set(average);
        });
    }
}
