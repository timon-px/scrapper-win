package com.desktop.services.config.constants;

import com.desktop.services.config.enums.SaveAsEnum;

import java.util.Map;

public class PathHelperConstants {
    public static final String TIKA_CONFIG_FOLDER = "tika";
    public static final String TIKA_CONFIG_FONT_FILE = "custom-mimetypes.xml";

    public static final Map<SaveAsEnum, String> MAP = Map.of(
            SaveAsEnum.ASSET, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.IMAGES, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.STYLESHEET, ScrapperConstants.STYLESHEET_FOLDER,
            SaveAsEnum.FONT, ScrapperConstants.FONT_FOLDER,
            SaveAsEnum.SCRIPT, ScrapperConstants.SCRIPTS_FOLDER,
            SaveAsEnum.VIDEO, ScrapperConstants.VIDEO_FOLDER,
            SaveAsEnum.AUDIO, ScrapperConstants.AUDIO_FOLDER);
}
