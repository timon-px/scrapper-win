package com.desktop.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesWorker {
    private static final Logger log = LoggerFactory.getLogger(PropertiesWorker.class);
    private final static Properties properties = new Properties();

    static {
        try {
            InputStream configStream = PropertiesWorker.class.getResourceAsStream("/config.properties");
            properties.load(configStream);
        } catch (Exception e) {
            log.error("Unable to load properties file");
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
