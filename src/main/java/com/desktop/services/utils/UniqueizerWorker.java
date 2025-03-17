package com.desktop.services.utils;

import com.desktop.services.config.constants.UniqueizerConstants;

import java.security.SecureRandom;

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

}
