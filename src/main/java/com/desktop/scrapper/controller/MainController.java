package com.desktop.scrapper.controller;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.scrapper.utils.FileExplorerHelper;
import com.desktop.scrapper.validation.MainValidation;
import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.services.services.classes.ScrapperService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Button browse_btn;
    @FXML
    private Button submit_btn;

    @FXML
    private Label dir_path_error_lbl;
    @FXML
    private Label web_url_error_lbl;

    @FXML
    private TextField dir_path_tf;
    @FXML
    private TextField web_url_tf;

    @FXML
    private ProgressBar progress_bar;
    @FXML
    private CheckBox replace_to_offer_cbx;

    @FXML
    private void initialize() {
        dirPathTFInit(dir_path_tf);
        browseBtnInit(browse_btn);
        progressBarInit(progress_bar);
        submitBtnInit(submit_btn);
    }

    private void submitBtnInit(Button button) {
        button.setOnAction(event -> handleSubmit());
    }

    private void progressBarInit(ProgressBar progressBar) {
        progressBar.setVisible(false);
    }

    private void browseBtnInit(Button button) {
        button.setOnAction(actionEvent -> {
            Scene currentScene = button.getScene();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose directory");

            var file = directoryChooser.showDialog(currentScene.getWindow());
            if (file != null) {
                dir_path_tf.setText(file.getAbsolutePath());
            }
        });
    }

    private void dirPathTFInit(TextField dirPathTF) {
        FileSystemView view = FileSystemView.getFileSystemView();
        File file = view.getHomeDirectory();
        dirPathTF.setText(file.getAbsolutePath());
    }

    private void handleSubmit() {
        String webUrl = web_url_tf.getText().trim();
        String dirPath = dir_path_tf.getText().trim();
        boolean isReplace = replace_to_offer_cbx.isSelected();

        if (!validateFields(dirPath, webUrl)) return;
        Path folderPath = Paths.get(dirPath);

        ScrapperService scrapperService = new ScrapperService();
        initSubmitAction(scrapperService);

        CompletableFuture<ScrapperResponseDTO> future = scrapperService.GetWeb(new ScrapperRequestDTO(folderPath, webUrl, isReplace));

        future.thenAccept(response -> Platform.runLater(() -> {
            String responseMessage = response.getMessage();
            successSubmitAction(responseMessage);
        })).exceptionally(throwable -> {
            Platform.runLater(() -> errorSubmitAction(throwable.getMessage()));
            return null;
        });
    }

    private boolean validateFields(String dirPath, String webUrl) {
        String dirError = MainValidation.validatePathField(dirPath);
        String urlError = MainValidation.validateUrlField(webUrl);

        dir_path_error_lbl.setText(dirError != null ? dirError : "");
        web_url_error_lbl.setText(urlError != null ? urlError : "");

        return dirError == null && urlError == null;
    }

    private void initSubmitAction(ScrapperService scrapperService) {
        submit_btn.setDisable(true);
        browse_btn.setDisable(true);
        progress_bar.setVisible(true);
        progress_bar.setProgress(0);

        progress_bar.progressProperty().bind(scrapperService.progressProperty());
    }

    private void successSubmitAction(String responseMessage) {
        Path responsePath = Paths.get(responseMessage);

        submit_btn.setDisable(false);
        browse_btn.setDisable(false);
        progress_bar.setVisible(false);

        progress_bar.progressProperty().unbind();
        Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION,
                "Done",
                "Website has successfully parsed!",
                "Do You want to open folder: " + responseMessage + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            openDownloadedFolder(responsePath);
        }
    }

    private void errorSubmitAction(String responseMessage) {
        progress_bar.progressProperty().unbind(); // Unbind when done
        submit_btn.setDisable(false);
        browse_btn.setDisable(false);
        progress_bar.setVisible(false);
        showAlert(Alert.AlertType.ERROR, "Error!", "Something went wrong!", responseMessage);
    }

    private Optional<ButtonType> showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait();
    }

    private void openDownloadedFolder(Path folderPath) {
        FileExplorerHelper.OpenFolderAsync(folderPath)
                .thenRun(() -> log.info("Folder opened successfully: {}", folderPath))
                .exceptionally(throwable -> {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to Open Folder", throwable.getMessage()));
                    return null;
                });
    }
}
