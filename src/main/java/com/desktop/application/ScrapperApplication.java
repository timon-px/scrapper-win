package com.desktop.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ScrapperApplication extends Application {
    static final String ASSETS_DIR = "/assets/";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ScrapperApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 540);
        setStylesheets(scene, "index.css");

        stage.setTitle("Uniqueizer");
        setFavicon(stage);

        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void setStylesheets(Scene scene, String stylesheet) {
        URL cssPath = this.getClass().getResource(ASSETS_DIR + stylesheet);
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath.toExternalForm());
        }
    }

    public void setFavicon(Stage stage) {
        URL faviconPath = this.getClass().getResource(ASSETS_DIR + "favicon.png");
        if (faviconPath != null) {
            stage.getIcons().add(new Image(faviconPath.toExternalForm()));
        }
    }
}