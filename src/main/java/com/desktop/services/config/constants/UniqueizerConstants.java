package com.desktop.services.config.constants;

import java.util.List;
import java.util.Set;

public class UniqueizerConstants {
    public static final String UNIQUE_SUFFIX_NAME = "_unique";
    public static final int RANDOM_STRING_LENGTH = 10;
    public static final int MIN_EMPTY_DIVS = 1;
    public static final int MAX_EMPTY_DIVS = 5;

    public static final Set<String> OG_PROPERTIES = Set.of("og:title", "og:description", "og:type", "og:url", "og:image");
    public static final Set<String> EXCLUDED_TAGS = Set.of("html", "head", "body", "script", "style", "link", "meta", "img");
    public static final List<String> DATA_ATTRS = List.of("data-changed", "data-modified", "data-updated");
    public static final List<String> PREFIXES = List.of("coz-", "moz-", "loz-", "taz-", "kaz-");

    public static final String ALLOWED_TO_RANDOM_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789";
}
