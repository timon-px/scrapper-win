package com.desktop.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class YamlKeywordLoader {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private static class KeywordFile {
        public List<String> keywords;
    }

    /**
     * Loads keyword list from a YAML file located in the classpath (resources).
     *
     * @param resourcePath Path to the resource file, e.g. "src_keywords.yml"
     * @return A Set of keyword strings
     * @throws IOException If the file is missing or unreadable
     */
    public static Set<String> LoadKeywordsFromResource(String resourcePath) throws IOException {
        Objects.requireNonNull(resourcePath, "Resource path cannot be null");

        if (resourcePath.startsWith("/")) resourcePath = resourcePath.substring(1);

        try (InputStream inputStream = YamlKeywordLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            KeywordFile keywordFile = yamlMapper.readValue(inputStream, KeywordFile.class);

            if (keywordFile.keywords == null || keywordFile.keywords.isEmpty()) {
                throw new IOException("No 'keywords' field found or list is empty in YAML: " + resourcePath);
            }

            return new HashSet<>(keywordFile.keywords);
        } catch (IOException e) {
            throw new IOException("Failed to load keywords from " + resourcePath + ": " + e.getMessage(), e);
        }
    }
}

