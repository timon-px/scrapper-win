package com.desktop.core.common.constants;

public class DriverConstants {
    public static final String DRIVER_RESOURCE_FOLDER = "driver";

    public static final String DRIVER_PROFILE_FOLDER = "profile";
    public static final String DRIVER_EXTENSION_FILE = "capture-extension.crx";

    public static final String DRIVER_EXTENSION_PATH = String.format("%s/%s",
            DriverConstants.DRIVER_RESOURCE_FOLDER, DriverConstants.DRIVER_EXTENSION_FILE);

    public static final String DRIVER_PROFILE_PATH = String.format("%s/%s",
            DriverConstants.DRIVER_RESOURCE_FOLDER, DriverConstants.DRIVER_PROFILE_FOLDER);
}
