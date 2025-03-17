package com.desktop.services.config.constants;

import java.util.Arrays;
import java.util.List;

public class HtmlConstants {
    private static final List<String> simpleSaveAttr = Arrays.asList("src", "href", "poster", "content", "data-lazy-src");
    private static final List<String> complexSaveAttr = Arrays.asList("srcset", "data-lazy-srcset");

    public static List<String> getSimpleSaveAttr() {
        return simpleSaveAttr;
    }
    public static List<String> getComplexSaveAttr() {
        return complexSaveAttr;
    }
}
