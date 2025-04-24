package com.desktop.core.common.constants;

import com.desktop.core.utils.YamlKeywordLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class HtmlConstants {
    public static final List<String> SIMPLE_SAVE_ATTR = Arrays.asList("src", "href", "poster", "content", "data-lazy-src");
    public static final List<String> COMPLEX_SAVE_ATTR = Arrays.asList("srcset", "data-lazy-srcset");

    public static final String LINK_STYLESHEETS_QUERY = "link[rel*='stylesheet'][href]";
    public static final String INLINE_STYLESHEETS_QUERY = "*[style]";
    public static final String BLOCK_STYLESHEETS_QUERY = "style";
    public static final String EXTERNAL_SCRIPTS_QUERY = "script[src], script[data-rocket-src]";
    public static final String HTML_IMAGE_QUERY = "img";
    public static final String NODE_WITH_CLASS_QUERY = "*[class]";
    public static final String ANCHOR_WITH_HREF_QUERY = "a[href]";
    public static final String EXTERNAL_FILES_QUERY = "*[src]:not(script, iframe), " +
            "*[data-lazy-src]:not(script, iframe), " +
            "*[data-lazy-srcset], " +
            "*[srcset], " +
            "*[poster]";

    public static final String UNIQUEIZER_META_TAGS_QUERY;
    public static final String UNIQUEIZER_ALL_TAGS_QUERY;

    public static final String FILTER_TAGS_QUERY;
    public static final Set<String> FILTER_SRC_KEYWORDS;
    public static final Set<String> FILTER_SCRIPT_KEYWORDS;

    private static final String FILTER_TAGS_RESOURCE_PATH = "/scrapper/meta_keywords.yaml";
    private static final String FILTER_SRC_RESOURCE_PATH = "/scrapper/src_keywords.yaml";
    private static final String FILTER_INLINE_RESOURCE_PATH = "/scrapper/inline_keywords.yaml";

    private static String getUniqueizerMetaTags() {
        int id = 0, length = UniqueizerConstants.OG_PROPERTIES.size();
        StringBuilder builder = new StringBuilder();

        for (String prop : UniqueizerConstants.OG_PROPERTIES) {
            builder.append("meta").append("[").append("property=").append(prop).append("]");
            if (++id < length) builder.append(", ");
        }

        return builder.toString();
    }

    private static String getFilterSelector(String path, String placeholder) {
        try {
            Set<String> filterTagSelector = YamlKeywordLoader.LoadKeywordsFromResource(path);
            return String.join(", ", filterTagSelector);
        } catch (IOException e) {
            return placeholder; // to not return completely empty
        }
    }

    private static Set<String> getFilterKeywords(String path, String placeholder) {
        try {
            return YamlKeywordLoader.LoadKeywordsFromResource(path);
        } catch (IOException e) {
            return Set.of(placeholder); // to not return completely empty
        }
    }

    static {
        UNIQUEIZER_ALL_TAGS_QUERY = "*:not(" + String.join(", ", UniqueizerConstants.EXCLUDED_TAGS) + ")";
        UNIQUEIZER_META_TAGS_QUERY = getUniqueizerMetaTags();

        FILTER_TAGS_QUERY = getFilterSelector(FILTER_TAGS_RESOURCE_PATH, "meta[name=url]");
        FILTER_SRC_KEYWORDS = getFilterKeywords(FILTER_SRC_RESOURCE_PATH, "connect.facebook.net");
        FILTER_SCRIPT_KEYWORDS = getFilterKeywords(FILTER_INLINE_RESOURCE_PATH, "www.google-analytics.com");
    }
}
