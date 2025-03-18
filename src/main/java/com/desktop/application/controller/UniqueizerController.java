package com.desktop.application.controller;

import com.desktop.application.controller.worker.ControllerWorker;
import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import com.desktop.application.validation.UniqueizerValidation;
import com.desktop.dto.UniqueizerRequestDTO;
import com.desktop.dto.UniqueizerResponseDTO;
import com.desktop.services.services.classes.UniqueizerService;
import com.desktop.services.services.interfaces.IUniqueizerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UniqueizerController {
    private static final Logger log = LoggerFactory.getLogger(UniqueizerController.class);
    private static final IControllerWorker controllerWorker = new ControllerWorker();
    private static List<Node> disableNodes;

    @FXML
    private VBox v_box;

    @FXML
    private Button browse_file_btn;
    @FXML
    private Button browse_save_btn;
    @FXML
    private Button submit_btn;

    @FXML
    private Label file_path_error_lbl;
    @FXML
    private Label save_path_error_lbl;

    @FXML
    private TextField file_path_tf;
    @FXML
    private TextField save_path_tf;

    @FXML
    private ProgressBar progress_bar;
    @FXML
    private CheckBox replace_to_offer_cbx;

    @FXML
    private void initialize() {
        disableNodes = List.of(browse_file_btn, browse_save_btn, submit_btn, file_path_tf, save_path_tf, replace_to_offer_cbx);

        progressBarInit(progress_bar);
        submitBtnInit(submit_btn);

        browseFileBtnInit(browse_file_btn, file_path_tf, save_path_tf);
        browseDirectoryBtnInit(browse_save_btn, save_path_tf);

        dragNDropInit(v_box);
    }

    private void dragNDropInit(VBox vbox) {
        vbox.setOnDragOver(evt -> {
            if (evt.getDragboard().hasFiles() && evt.getDragboard().getFiles().size() == 1) {
                evt.acceptTransferModes(TransferMode.LINK);
            }
        });
        vbox.setOnDragDropped(evt -> {
            File file = evt.getDragboard().getFiles().getFirst();
            String validation = UniqueizerValidation.validateFilePathField(file.getAbsolutePath());
            controllerWorker.SetErrorLabel(file_path_error_lbl, validation);

            chooseFile(file_path_tf, save_path_tf, file);
        });
    }

    private void submitBtnInit(Button button) {
        button.setOnAction(event -> handleSubmit());
    }

    private void progressBarInit(ProgressBar progressBar) {
        progressBar.setVisible(false);
    }

    private void browseFileBtnInit(Button button, TextField textField, TextField relativeTextField) {
        controllerWorker.InitFileBrowseBtn(button, file -> chooseFile(textField, relativeTextField, file));
    }

    private static void chooseFile(TextField textField, TextField relativeTextField, File file) {
        textField.setText(file.getAbsolutePath());
        relativeTextField.setText(file.getParentFile().getAbsolutePath());
    }

    private void browseDirectoryBtnInit(Button button, TextField textField) {
        controllerWorker.InitDirectoryBrowseBtn(button, file -> textField.setText(file.getAbsolutePath()));
    }

    private void handleSubmit() {
        String filePath = file_path_tf.getText().trim();
        String savePath = save_path_tf.getText().trim();
        boolean isReplaceSelected = replace_to_offer_cbx.isSelected();

        if (!validateFields(filePath, savePath)) return;

        Path fileDir = Paths.get(filePath);
        Path saveDir = Paths.get(savePath);
        File file = fileDir.toFile();

        IUniqueizerService uniqueizerService = new UniqueizerService();
        initSubmitAction(uniqueizerService);

        CompletableFuture<UniqueizerResponseDTO> future = uniqueizerService.UniqueizeWeb(new UniqueizerRequestDTO(file, saveDir, isReplaceSelected));
        future.thenAccept(response -> Platform.runLater(() -> {
            String responseMessage = response.getMessage();
            successSubmitAction(responseMessage);
        })).exceptionally(throwable -> {
            Platform.runLater(() -> errorSubmitAction(throwable.getMessage()));
            return null;
        });
    }

    private boolean validateFields(String filePath, String savePath) {
        String fileError = UniqueizerValidation.validateFilePathField(filePath);
        String saveError = UniqueizerValidation.validateSavePathField(savePath);

        controllerWorker.SetErrorLabel(file_path_error_lbl, fileError);
        controllerWorker.SetErrorLabel(save_path_error_lbl, saveError);

        return fileError == null && saveError == null;
    }

    private void initSubmitAction(IUniqueizerService uniqueizerService) {
        controllerWorker.SetLoading(true, disableNodes, progress_bar);

        progress_bar.setProgress(0);
        progress_bar.progressProperty().bind(uniqueizerService.progressProperty());
    }

    private void successSubmitAction(String responseMessage) {
        controllerWorker.SetLoading(false, disableNodes, progress_bar);
        progress_bar.progressProperty().unbind();

        Optional<ButtonType> result = controllerWorker.ShowAllert(Alert.AlertType.CONFIRMATION,
                "Done",
                "Website has successfully unified!",
                "Do You want to open folder with file:\n" + responseMessage + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Path responsePath = Paths.get(responseMessage);
                controllerWorker.OpenDownloadedFolder(responsePath);
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void errorSubmitAction(String responseMessage) {
        controllerWorker.SetLoading(false, disableNodes, progress_bar);
        progress_bar.progressProperty().unbind(); // Unbind when done
        controllerWorker.ShowAllert(Alert.AlertType.ERROR, "Error!", "Something went wrong!", responseMessage);
    }
}