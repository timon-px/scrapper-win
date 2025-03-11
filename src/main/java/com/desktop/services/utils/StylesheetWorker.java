package com.desktop.services.utils;

import com.desktop.services.config.constants.RegexConstants;

import java.util.function.Function;
import java.util.regex.Matcher;

public class StylesheetWorker {
    public static String ProcessDependencies(String cssContent, Function<String, String> processImports, Function<String, String> processExternal) {
        return processUrls(cssContent, processImports, processExternal);
    }

    private static String processUrls(String cssContent, Function<String, String> processImports, Function<String, String> processExternal) {
        return RegexWorker.
                ProcessUrlsByRegex(cssContent,
                        RegexConstants.DEPENDENCIES_CSS_URL_REGEX,
                        (matcher -> processUrlsMatcher(matcher, processImports, processExternal)));
    }

    private static String processUrlsMatcher(Matcher matcher, Function<String, String> processImports, Function<String, String> processExternal) {
        if (matcher.group(1) != null) {
            // This is an @import match
            String url = matcher.group(2); // URL from @import
            return processStylesheetMatchers(url, processImports);
        } else if (matcher.group(3) != null) {
            // This is an external url() match
            String url = matcher.group(5); // URL from url()
            return processStylesheetMatchers(url, processExternal);
        }

        return matcher.group(0); // Fallback (shouldn’t happen)
    }

    private static String processStylesheetMatchers(String str, Function<String, String> processFunc) {
        if (str == null || str.isEmpty()) return null;

        try {
            return processFunc.apply(str);
        } catch (Exception e) {
            return null;
        }
    }
}
