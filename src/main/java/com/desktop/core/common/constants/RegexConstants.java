package com.desktop.core.common.constants;

public class RegexConstants {
    public static final String STYLESHEET_URL_REGEX = "url\\(\\s*(['\\\"]?)(?!data:)\\s*([^'\\\"()]+?\\.[a-zA-Z0-9]+(?:[?#][^'\\\"()]+?)?)\\s*\\1\\s*\\)";
    public static final String IS_ABSOLUTE_URL_REGEX = "^(?:[a-z+]+:)?//";
    public static final String SRCSET_URL_REGEX = "(https?://[^\\s,]+|[^\\s,]+?\\.\\w+)(?:\\s+\\d*(?:\\.\\d+)?[wx])?";
    public static final String IMPORT_CSS_URL_REGEX = "@import\\s+(?:url\\(\\s*['\"]?|['\"])([^'\"\\s)]+)(?:['\"]?\\s*[\\)|\\\"])?";
    public static final String DEPENDENCIES_CSS_URL_REGEX = "(@import\\s+(?:url\\(\\s*['\"]?|['\"])([^'\"\\s)]+)(?:['\"]?\\s*[\\)|\\\"])?)|(url\\(\\s*(['\\\"]?)(?!data:)\\s*([^'\\\"()]+?\\.[a-zA-Z0-9]+(?:[?#][^'\\\"()]+?)?)\\s*\\4\\s*\\))";

    public static final String DATA_REPEAT_MATCH_REGEX = "(" + String.join("|", UniqueizerConstants.PREFIXES) + ")" + "([^\\s]*)";
    public static final String DATA_REPEAT_CHECK_REGEX = "^" + DATA_REPEAT_MATCH_REGEX + "$";
}
