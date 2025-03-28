package com.desktop.services.config.constants;

import com.desktop.services.config.enums.SaveAsEnum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScrapperWorkerConstants {
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
