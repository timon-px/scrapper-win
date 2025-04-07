package com.desktop.core.scrapper.processor.interfaces;

import com.desktop.core.common.model.FileSaveModel;
import javafx.beans.property.DoubleProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public interface IFilesProcess {
    CompletableFuture<Void> SaveAsync(ConcurrentHashMap<String, FileSaveModel> filesToSave, DoubleProperty progress);
}
