package com.desktop.application.controller.worker;

import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import com.desktop.application.utils.AlertUtils;
import com.desktop.application.utils.FileExplorerUtils;
import com.desktop.core.common.constants.UniqueizerConstants;
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
    public void InitFileBrowseBtn(Button button, Consumer<List<File>> callback) {
        button.setOnAction(actionEvent -> {
            Scene currentScene = button.getScene();

            FileChooser.ExtensionFilter extensionFilter = new FileChooser
                    .ExtensionFilter("HTML Files", "*.html", "*.htm", "*.php");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose HTML File");
            fileChooser.getExtensionFilters().add(extensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(currentScene.getWindow());

            if (files != null && !files.isEmpty()) {
                callback.accept(files);
            }
        });
    }

    @Override
    public String GetStringFromFiles(List<File> files) {
        List<String> selectedFiles = files.stream().map(File::getAbsolutePath).toList();
        return String.join(UniqueizerConstants.FILE_LIST_SEPARATOR, selectedFiles);
    }

    @Override
    public List<File> GetFilesFromString(String filesString) {
        List<String> selectedPaths = List.of(filesString.split(","));
        return selectedPaths.stream().map(path -> new File(path.trim())).toList();
    }

    @Override
    public Optional<ButtonType> ShowAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = AlertUtils.CreateAlert(type, title, header, content);
        return alert.showAndWait();
    }

    @Override
    public void OpenDownloadedFolderDialog(Path directory, String message) {
        Optional<ButtonType> result = ShowAlert(Alert.AlertType.CONFIRMATION,
                "Success!",
                message,
                "Do You want to open folder:\n" + directory + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                OpenDownloadedFolder(directory);
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void OpenDownloadedFolder(Path folderPath) {
        FileExplorerUtils.OpenFolderAsync(folderPath)
                .thenRun(() -> log.info("Folder opened successfully: {}", folderPath))
                .exceptionally(throwable -> {
                    Platform.runLater(() ->
                            ShowAlert(Alert.AlertType.ERROR, "Error!", "Failed to Open Folder", throwable.getMessage()));
                    return null;
                });
    }
}
