package com.desktop.application;

import com.desktop.application.utils.AlertUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class ScrapperApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(ScrapperApplication.class);

    private static final String ASSETS_DIR = "/assets/";
    private static final String MAIN_FXML = "main-view.fxml";
    private static final String[] MAIN_STYLESHEETS = {"index.css", "fonts.css"};
    private static final String FAVICON_PATH = ASSETS_DIR + "icons/favicon.png";

    @Override
    public void start(Stage stage) {
        // Show splash screen
        var splashScreen = new SplashScreen();
        splashScreen.show();

        // Loading the main window in a background thread
        Thread loadMainUiThread = new Thread(() -> {
            try {
                Scene mainScene = loadMainScene();
                Platform.runLater(() -> {
                    configurePrimaryStage(stage, mainScene);

                    stage.setOnShown(event -> splashScreen.close());
                    stage.show();
                });
            } catch (IOException e) {
                log.error("Failed to load main UI", e);
                Platform.runLater(() -> {
                    splashScreen.close();
                    showErrorDialog(e.getMessage());
                });
            }
        }, "Main-UI-Loader");

        loadMainUiThread.setDaemon(true);
        loadMainUiThread.start();
    }

    /**
     * Loads the main scene from FXML and applies stylesheets.
     *
     * @return The loaded Scene.
     * @throws IOException If FXML or resources cannot be loaded.
     */
    private Scene loadMainScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML));
        Scene scene = new Scene(fxmlLoader.load(), 720, 540);
        scene.setFill(Color.TRANSPARENT);

        for (String stylesheet : MAIN_STYLESHEETS) {
            applyStylesheet(scene, stylesheet);
        }
        return scene;
    }

    /**
     * Configures the primary stage with title, favicon, and size constraints.
     *
     * @param stage The primary stage.
     * @param scene The scene to set.
     */
    private void configurePrimaryStage(Stage stage, Scene scene) {
        applyFavicon(stage);

        stage.setTitle("Uniqueizer");
        stage.setScene(scene);

        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);

        stage.setMinWidth(680);
        stage.setMinHeight(520);
    }

    /**
     * Applies a stylesheet to the given scene if it exists.
     *
     * @param scene      The scene to apply the stylesheet to.
     * @param stylesheet The stylesheet filename (relative to ASSETS_DIR).
     */
    private void applyStylesheet(Scene scene, String stylesheet) {
        URL cssPath = getClass().getResource(ASSETS_DIR + stylesheet);
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath.toExternalForm());
        } else {
            log.warn("Stylesheet not found: {}{}", ASSETS_DIR, stylesheet);
        }
    }

    /**
     * Sets the favicon for the given stage if it exists.
     *
     * @param stage The stage to apply the favicon to.
     */
    private void applyFavicon(Stage stage) {
        URL faviconPath = getClass().getResource(FAVICON_PATH);
        if (faviconPath != null) {
            stage.getIcons().add(new Image(faviconPath.toExternalForm()));
        } else {
            log.warn("Favicon not found: {}", FAVICON_PATH);
        }
    }

    /**
     * Shows an error alert if the main UI fails to load.
     *
     * @param message The error message.
     */
    private void showErrorDialog(String message) {
        Alert alert = AlertUtils.CreateAlert(Alert.AlertType.ERROR,
                "Startup error!",
                "The application could not be launched.",
                "Error has been occurred: " + message);

        alert.setOnCloseRequest(event -> Platform.exit());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // for proper font displaying
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        launch();
    }
}