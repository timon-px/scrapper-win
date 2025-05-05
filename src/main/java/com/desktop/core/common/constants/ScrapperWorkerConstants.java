package com.desktop.core.common.constants;

import com.desktop.core.common.enums.SaveAsEnum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScrapperWorkerConstants {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5756.197 Safari/537.36";
    public static final Character[] INVALID_SPECIFIC_CHARS = {'"', '*', '<', '>', '?', '|', '#', ':'};

    public static final Set<SaveAsEnum> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            SaveAsEnum.AUDIO,
            SaveAsEnum.VIDEO,
            SaveAsEnum.FONT,
            SaveAsEnum.IMAGES));

    public static final Map<String, SaveAsEnum> CONTENT_TYPE_TO_SAVE_AS = Map.of(
            "text/css", SaveAsEnum.STYLESHEET,
            "text/javascript", SaveAsEnum.SCRIPT,
            "font/", SaveAsEnum.FONT,
            "video/", SaveAsEnum.VIDEO,
            "image/", SaveAsEnum.IMAGES,
            "audio/", SaveAsEnum.AUDIO
    );
}
