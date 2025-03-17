package com.desktop.application.controller.worker.interfaces;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface IControllerWorker {
    void SetErrorLabel(Label label, String error);

    void SetLoading(boolean isLoading, List<Node> disableNodes, ProgressBar progressBar);

    void InitDirectoryBrowseBtn(Button button, Consumer<File> callback);

    void InitFileBrowseBtn(Button button, Consumer<File> callback);

    Optional<ButtonType> ShowAllert(Alert.AlertType type, String title, String header, String content);

    void OpenDownloadedFolder(Path folderPath);
}
