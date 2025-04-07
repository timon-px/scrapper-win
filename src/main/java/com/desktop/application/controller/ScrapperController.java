package com.desktop.application.controller;

import com.desktop.application.controller.worker.ControllerWorker;
import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import com.desktop.application.validation.ScrapperValidation;
import com.desktop.core.common.dto.ScrapperRequestDTO;
import com.desktop.core.common.dto.ScrapperResponseDTO;
import com.desktop.core.api.ScrapperService;
import com.desktop.core.api.interfaces.IScrapperService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScrapperController {
    private static final Logger log = LoggerFactory.getLogger(ScrapperController.class);
    private static final IControllerWorker controllerWorker = new ControllerWorker();
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
    private CheckBox process_driver_cbx;

    @FXML
    private void initialize() {
        disableNodes = List.of(browse_btn, submit_btn, dir_path_tf, web_url_tf, replace_to_offer_cbx, process_driver_cbx);

        dirPathTfInit(dir_path_tf);
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
        controllerWorker.InitDirectoryBrowseBtn(button, file -> dir_path_tf.setText(file.getAbsolutePath()));
    }

    private void dirPathTfInit(TextField dirPathTf) {
        FileSystemView view = FileSystemView.getFileSystemView();
        File file = view.getHomeDirectory();
        dirPathTf.setText(file.getAbsolutePath());
    }

    private void handleSubmit() {
        String webUrl = web_url_tf.getText().trim();
        String dirPath = dir_path_tf.getText().trim();

        if (!validateFields(dirPath, webUrl)) return;
        Path folderPath = Paths.get(dirPath);

        IScrapperService scrapperService = new ScrapperService();
        initSubmitAction(scrapperService);

        ScrapperRequestDTO.ProcessingOptions processingOptions = getProcessingOptions();
        CompletableFuture<ScrapperResponseDTO> future = scrapperService.GetWeb(new ScrapperRequestDTO(folderPath, webUrl, processingOptions));
        future.thenAccept(response -> Platform.runLater(() -> {
            Path directory = response.getDirectory();
            String message = response.getMessage();

            if (response.isSuccess())
                successSubmitAction(directory, message);
            else
                errorPlatformRun(new Throwable(message));
        })).exceptionally(this::errorPlatformRun);
    }

    private ScrapperRequestDTO.ProcessingOptions getProcessingOptions() {
        boolean shouldReplaceHref = replace_to_offer_cbx.isSelected();
        boolean shouldProcessDriver = process_driver_cbx.isSelected();
        return new ScrapperRequestDTO
                .ProcessingOptions(shouldReplaceHref, shouldProcessDriver, true);
    }

    private boolean validateFields(String dirPath, String webUrl) {
        String dirError = ScrapperValidation.validatePathField(dirPath);
        String urlError = ScrapperValidation.validateUrlField(webUrl);

        controllerWorker.SetErrorLabel(dir_path_error_lbl, dirError);
        controllerWorker.SetErrorLabel(web_url_error_lbl, urlError);

        return dirError == null && urlError == null;
    }

    private void initSubmitAction(IScrapperService scrapperService) {
        controllerWorker.SetLoading(true, disableNodes, progress_bar);

        progress_bar.setProgress(0);
        progress_bar.progressProperty().bind(scrapperService.progressProperty());
    }

    private void successSubmitAction(Path directory, String message) {
        controllerWorker.SetLoading(false, disableNodes, progress_bar);
        progress_bar.progressProperty().unbind();

        controllerWorker.OpenDownloadedFolderDialog(directory, message);
    }

    private void errorSubmitAction(String responseMessage) {
        controllerWorker.SetLoading(false, disableNodes, progress_bar);
        progress_bar.progressProperty().unbind(); // Unbind when done
        controllerWorker.ShowAllert(Alert.AlertType.ERROR, "Error!", "Something went wrong!", responseMessage);
    }

    private Void errorPlatformRun(Throwable throwable) {
        Platform.runLater(() -> errorSubmitAction(throwable.getMessage()));
        return null;
    }
}
