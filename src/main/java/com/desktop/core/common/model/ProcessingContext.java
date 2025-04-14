package com.desktop.core.common.model;

import com.desktop.core.common.dto.ScrapperRequestDTO;
import javafx.beans.property.SimpleDoubleProperty;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessingContext {
    private final Path savePath;
    private final ScrapperRequestDTO.ProcessingOptions options;
    private final ConcurrentHashMap<String, String> usedFileNamesCssList;
    private final ConcurrentHashMap<String, FileSaveModel> filesToSaveList;
    private final SimpleDoubleProperty progress;

    public ProcessingContext(Path savePath, SimpleDoubleProperty progress, ScrapperRequestDTO.ProcessingOptions options) {
        this.savePath = savePath;
        this.progress = progress;
        this.options = options;
        this.usedFileNamesCssList = new ConcurrentHashMap<>();
        this.filesToSaveList = new ConcurrentHashMap<>();
    }

    public Path getSavePath() {
        return savePath;
    }

    public ScrapperRequestDTO.ProcessingOptions getOptions() {
        return options;
    }

    public double getProgress() {
        return progress.get();
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    public ConcurrentHashMap<String, String> getUsedFileNamesCssList() {
        return usedFileNamesCssList;
    }

    public ConcurrentHashMap<String, FileSaveModel> getFilesToSaveList() {
        return filesToSaveList;
    }
}
