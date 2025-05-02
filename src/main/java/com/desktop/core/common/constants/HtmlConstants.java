package com.desktop.core.common.constants;

import java.util.Arrays;
import java.util.List;

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

    private static String getUniqueizerMetaTags() {
        int id = 0, length = UniqueizerConstants.OG_PROPERTIES.size();
        StringBuilder builder = new StringBuilder();

        for (String prop : UniqueizerConstants.OG_PROPERTIES) {
            builder.append("meta").append("[").append("property=").append(prop).append("]");
            if (++id < length) builder.append(", ");
        }

        return builder.toString();
    }

    static {
        UNIQUEIZER_ALL_TAGS_QUERY = "*:not(" + String.join(", ", UniqueizerConstants.EXCLUDED_TAGS) + ")";
        UNIQUEIZER_META_TAGS_QUERY = getUniqueizerMetaTags();
    }
}
