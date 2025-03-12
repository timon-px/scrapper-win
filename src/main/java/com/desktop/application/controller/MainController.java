package com.desktop.application.controller;

import com.desktop.application.controller.worker.ScrapperControllerWorker;
import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import com.desktop.application.utils.FileExplorerHelper;
import com.desktop.application.validation.MainValidation;
import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.services.services.classes.ScrapperService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private static final IControllerWorker scrapperControllerWorker = new ScrapperControllerWorker();
    private static List<Node> disableNodes;

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
    private TabPane tab_pane;

    @FXML
    private void initialize() {
        disableNodes = List.of(browse_btn, submit_btn, dir_path_tf, web_url_tf, replace_to_offer_cbx);

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
        boolean isReplaceSelected = replace_to_offer_cbx.isSelected();

        if (!validateFields(dirPath, webUrl)) return;
        Path folderPath = Paths.get(dirPath);

        ScrapperService scrapperService = new ScrapperService();
        initSubmitAction(scrapperService);

        CompletableFuture<ScrapperResponseDTO> future = scrapperService.GetWeb(new ScrapperRequestDTO(folderPath, webUrl, isReplaceSelected));

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

        scrapperControllerWorker.SetErrorLabel(dir_path_error_lbl, dirError);
        scrapperControllerWorker.SetErrorLabel(web_url_error_lbl, urlError);

        return dirError == null && urlError == null;
    }

    private void initSubmitAction(ScrapperService scrapperService) {
        scrapperControllerWorker.SetLoading(true, disableNodes, progress_bar);

        progress_bar.setProgress(0);
        progress_bar.progressProperty().bind(scrapperService.progressProperty());
    }

    private void successSubmitAction(String responseMessage) {
        Path responsePath = Paths.get(responseMessage);

        scrapperControllerWorker.SetLoading(false, disableNodes, progress_bar);

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
        scrapperControllerWorker.SetLoading(false, disableNodes, progress_bar);
        progress_bar.progressProperty().unbind(); // Unbind when done
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
