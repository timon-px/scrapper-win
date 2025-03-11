package com.desktop.services.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexWorker {
    /**
     * Replaces URLs in the based on the given regex and modification function.
     *
     * @param content           The content to process.
     * @param regexPattern      The regex pattern to match URLs.
     * @param modifyUrlFunction A function that defines how to modify each matched URL.
     * @return The modified content.
     */
    public static String ProcessUrlsByRegex(String content, String regexPattern, Function<Matcher, String> modifyUrlFunction) {
        Matcher matcher = initMatcher(content, regexPattern);
        StringBuilder modifiedContent = new StringBuilder();

        return processMatcher(matcher, modifiedContent, modifyUrlFunction).toString();
    }

    /**
     * Replaces URLs in the based on the given regex and modification function.
     *
     * @param content              The content to process.
     * @param regexPattern         The regex pattern to match URLs.
     * @param modifyUrlFunction    A function that defines how to modify each matched URL.
     * @param matcherStartPosition A number of the start position in matcher
     * @return The modified content.
     */
    public static String ProcessUrlsByRegex(String content, String regexPattern, Function<Matcher, String> modifyUrlFunction, Integer matcherStartPosition) {
        Matcher matcher = initMatcher(content, regexPattern);
        StringBuilder modifiedContent = new StringBuilder();

        if (matcherStartPosition > 0) {
            matcher.region(matcherStartPosition, content.length());
        }

        return processMatcher(matcher, modifiedContent, modifyUrlFunction).toString();
    }

    private static Matcher initMatcher(String content, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);
        return pattern.matcher(content);
    }

    private static StringBuilder processMatcher(Matcher matcher, StringBuilder modifiedContent, Function<Matcher, String> modifierFunction) {
        while (matcher.find()) {
            // Apply the custom modification function
            String modifiedUrl = modifierFunction.apply(matcher);
            if (modifiedUrl == null) continue;

            // Replace the matched URL with the modified version
            matcher.appendReplacement(modifiedContent, modifiedUrl);
        }

        // Append any remaining content after the last match
        matcher.appendTail(modifiedContent);

        return modifiedContent;
    }
}
