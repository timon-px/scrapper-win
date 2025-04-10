package com.desktop.core.common.dto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class UniqueizerRequestDTO {
    private final List<File> files;
    private final Path savePath;
    private final ProcessingOptions processingOptions;

    public UniqueizerRequestDTO(List<File> files, Path savePath, ProcessingOptions processingOptions) {
        this.files = files;
        this.savePath = savePath;
        this.processingOptions = processingOptions;
    }

    public List<File> getFiles() {
        return files;
    }

    public Path getSavePath() {
        return savePath;
    }

    public ProcessingOptions getProcessingOptions() {
        return processingOptions;
    }

    public static class ProcessingOptions {
        private final boolean replaceHref;
        private final boolean processChars;

        public ProcessingOptions(boolean replaceHref, boolean processChars) {
            this.replaceHref = replaceHref;
            this.processChars = processChars;
        }

        public boolean shouldReplaceHref() {
            return replaceHref;
        }

        public boolean shouldProcessChars() {
            return processChars;
        }
    }
}
