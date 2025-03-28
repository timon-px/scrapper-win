package com.desktop.dto;

import java.nio.file.Path;

public class ScrapperRequestDTO {
    private final Path directory;
    private final String url;
    private final ProcessingOptions processingOptions;

    public ScrapperRequestDTO(Path directory, String url, ProcessingOptions processingOptions) {
        this.directory = directory;
        this.url = url;
        this.processingOptions = processingOptions;
    }

    public Path getDirectory() {
        return directory;
    }


    public String getUrl() {
        return url;
    }

    public ProcessingOptions getProcessingOptions() {
        return processingOptions;
    }

    public static class ProcessingOptions {
        private final boolean replaceHref;
        private final boolean processDriver;
        private final boolean processDriverCustomStyles;

        public ProcessingOptions(boolean replaceHref, boolean processDriver, boolean processDriverCustomStyles) {
            this.replaceHref = replaceHref;
            this.processDriver = processDriver;
            this.processDriverCustomStyles = processDriverCustomStyles;
        }

        public boolean shouldReplaceHref() {
            return replaceHref;
        }

        public boolean shouldProcessDriver() {
            return processDriver;
        }

        public boolean shouldProcessDriverCustomStyles() {
            return processDriverCustomStyles;
        }
    }
}
