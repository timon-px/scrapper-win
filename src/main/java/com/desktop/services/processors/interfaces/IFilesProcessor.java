package com.desktop.services.processors.interfaces;

import com.desktop.services.models.FileSaveModel;
import javafx.beans.property.DoubleProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public interface IFilesProcessor {
    CompletableFuture<Void> SaveAsync(ConcurrentHashMap<String, FileSaveModel> filesToSave, DoubleProperty progress);
}
