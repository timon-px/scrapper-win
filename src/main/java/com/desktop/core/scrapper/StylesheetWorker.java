package com.desktop.core.scrapper;

import com.desktop.core.common.constants.RegexConstants;
import com.desktop.core.utils.RegexWorker;
import org.silentsoft.csscolor4j.Color;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

public class StylesheetWorker {
    public static Map<String, List<String>> GetStylesMap(String styles) {
        Map<String, List<String>> stylesMap = new HashMap<>();

        List<String> list = List.of(styles.split("[:;]"));
        for (int i = 0; i < list.size(); i += 2) {
            String styleRule = list.get(i).trim();

            int styleValueId = i + 1;
            if (styleValueId >= list.size())
                throw new IllegalArgumentException("Style list contains invalid style rule: " + styleRule);

            List<String> styleValues = List.of(list.get(i + 1).trim().split("\\b\\s"));

            if (styleRule.isBlank() || styleValues.isEmpty()) continue;
            stylesMap.put(styleRule, styleValues);
        }

        return stylesMap;
    }

    public static Map<String, List<String>> ProcessUniqueStylesColors(Map<String, List<String>> stylesMap) {
        Map<String, List<String>> uniqueStyles = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : stylesMap.entrySet()) {
            String key = entry.getKey();

            List<String> values = entry.getValue();
            List<String> newValues = processStyleValuesColor(values);

            uniqueStyles.put(key, newValues);
        }

        return uniqueStyles;
    }

    public static String GetStylesString(Map<String, List<String>> stylesMap) {
        List<String> styles = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : stylesMap.entrySet()) {
            String styleRule = entry.getKey();
            List<String> styleValues = entry.getValue();
            String styleValuesString = String.join(" ", styleValues);

            styles.add(styleRule + ": " + styleValuesString);
        }

        return String.join("; ", styles);
    }

    public static String ProcessDependencies(String cssContent, Function<String, String> processImports, Function<String, String> processExternal) {
        return processUrls(cssContent, processImports, processExternal);
    }

    private static String processUrls(String cssContent, Function<String, String> processImports, Function<String, String> processExternal) {
        return RegexWorker.
                ProcessStringByRegex(cssContent,
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

    private static List<String> processStyleValuesColor(List<String> styleValues) {
        List<String> newValues = new ArrayList<>();

        for (String value : styleValues) {
            try {
                Color decoded = Color.valueOf(value);
                String hex = processUniqueColor(decoded);
                newValues.add(hex);
            } catch (Exception e) {
                newValues.add(value);
            }
        }

        return newValues;
    }

    private static String processUniqueColor(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        double opacity = color.getOpacity();

        SecureRandom random = new SecureRandom();
        int randomInt = random.nextInt(1, 3);

        red = Math.min(255, Math.max(0, red + randomInt));
        green = Math.min(255, Math.max(0, green + randomInt));
        blue = Math.min(255, Math.max(0, blue + randomInt));

        return Color.rgb(red, green, blue, opacity).getHex();
    }
}
