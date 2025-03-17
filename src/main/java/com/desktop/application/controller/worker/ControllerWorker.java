package com.desktop.application.controller.worker;

import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import com.desktop.application.utils.FileExplorerHelper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ControllerWorker implements IControllerWorker {
    private static final Logger log = LoggerFactory.getLogger(ControllerWorker.class);

    @Override
    public void SetErrorLabel(Label label, String error) {
        label.setText("");
        if (error != null) label.setText(error);
    }

    @Override
    public void SetLoading(boolean isLoading, List<Node> disableNodes, ProgressBar progressBar) {
        for (Node nodes : disableNodes) {
            nodes.setDisable(isLoading);
        }

        progressBar.setVisible(isLoading);
    }

    @Override
    public void InitDirectoryBrowseBtn(Button button, Consumer<File> callback) {
        button.setOnAction(actionEvent -> {
            Scene currentScene = button.getScene();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose directory");

            File file = directoryChooser.showDialog(currentScene.getWindow());
            if (file != null) {
                callback.accept(file);
            }
        });
    }

    @Override
    public void InitFileBrowseBtn(Button button, Consumer<File> callback) {
        button.setOnAction(actionEvent -> {
            Scene currentScene = button.getScene();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file");

            File file = fileChooser.showOpenDialog(currentScene.getWindow());
            if (file != null) {
                callback.accept(file);
            }
        });
    }

    @Override
    public Optional<ButtonType> ShowAllert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait();
    }

    @Override
    public void OpenDownloadedFolder(Path folderPath) {
        FileExplorerHelper.OpenFolderAsync(folderPath)
                .thenRun(() -> log.info("Folder opened successfully: {}", folderPath))
                .exceptionally(throwable -> {
                    Platform.runLater(() ->
                            ShowAllert(Alert.AlertType.ERROR, "Error", "Failed to Open Folder", throwable.getMessage()));
                    return null;
                });
    }
}
