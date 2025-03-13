package com.desktop.services.utils;

import com.desktop.services.config.constants.UniqueizerConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.security.SecureRandom;

public class UniqueizerWorker {
    public static Elements ScrapProcessedMeta(Document document) {
        // Вибираємо всі <style> із SRC
        return document.select(getProcessMetaSelector());
    }

    public static String GetRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(10);
            randomString.append(number);
        }
        return randomString.toString();
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
}
