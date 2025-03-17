package com.desktop.services.utils;

import com.desktop.services.config.constants.UniqueizerConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.security.SecureRandom;

public class UniqueizerWorker {
    private static final SecureRandom random = new SecureRandom();
    
    public static Elements ScrapProcessedMeta(Document document) {
        // Вибираємо всі <meta> із og:
        return document.select(getProcessMetaSelector());
    }

    public static Elements ScrapProcessedTags(Document document) {
        // Вибираємо всі not excluded tags
        return document.children().select(getProcessTagsSelector());
    }

    public static Elements ScrapImgTags(Document document) {
        // Вибираємо всі images
        return document.select("img");
    }

    public static Elements ScrapTagsWithClasses(Document document) {
        // Вибираємо всі tags with classes
        return document.select("*[class]");
    }

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

    private static String getProcessMetaSelector() {
        int id = 0;
        int length = UniqueizerConstants.OG_PROPERTIES.size();
        StringBuilder builder = new StringBuilder();
        for (String prop : UniqueizerConstants.OG_PROPERTIES) {
            builder.append("meta").append("[").append("property=").append(prop).append("]");

            if (++id < length) builder.append(", ");
        }

        return builder.toString();
    }

    private static String getProcessTagsSelector() {
        String excludedTags = String.join(", ", UniqueizerConstants.EXCLUDED_TAGS);
        return "*" + ":not(" + excludedTags + ")";
    }
}
