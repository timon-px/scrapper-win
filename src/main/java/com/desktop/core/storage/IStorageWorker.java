package com.desktop.core.storage;

import com.desktop.core.common.model.FileSaveModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface IStorageWorker {

    Path GetFolderPath(String initName);

    CompletableFuture<Path> SaveContentAsync(String content, String fileName);

    CompletableFuture<Path> SaveContentAsync(String content, Path folderPath, String fileName);

    CompletableFuture<String> SaveFileAsync(String url, FileSaveModel fileSaveModel, Path mainPath);

    void InitFoldersAsync(Collection<FileSaveModel> fileSaveModels, Path mainPath) throws IOException;
}
