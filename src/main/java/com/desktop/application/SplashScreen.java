package com.desktop.application;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Manages the splash screen displayed during application startup.
 */
public class SplashScreen {
    private static final Logger log = LoggerFactory.getLogger(SplashScreen.class);

    private static final String ASSETS_DIR = "/assets/";
    private static final String SPLASH_CSS = "splash.css";
    private static final String LOGO_PATH = ASSETS_DIR + "icons/favicon-linux.png";
    private static final String FAVICON_PATH = ASSETS_DIR + "icons/favicon.png";
    private static final Integer DEFAULT_STAGE_WIDTH = 412;
    private static final Integer DEFAULT_STAGE_HEIGHT = 216;

    private final Stage splashStage;

    public SplashScreen() {
        this.splashStage = new Stage();
        initialize();
    }

    /**
     * Initializes the splash screen with layout, styles, and favicon.
     */
    private void initialize() {
        VBox splashLayout = new VBox();
        splashLayout.setAlignment(Pos.CENTER);

        applyLogo(splashLayout, LOGO_PATH);

        Scene splashScene = new Scene(splashLayout, DEFAULT_STAGE_WIDTH, DEFAULT_STAGE_HEIGHT);

        applyStylesheet(splashScene, SPLASH_CSS);
        applyFavicon();

        splashStage.setScene(splashScene);
        splashStage.initStyle(StageStyle.UNDECORATED);

        centerStageOnScreen();
    }

    /**
     * Centers the splash screen stage on the primary screen.
     */
    private void centerStageOnScreen() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = primaryScreenBounds.getWidth();
        double screenHeight = primaryScreenBounds.getHeight();

        double centerX = primaryScreenBounds.getMinX() + (screenWidth - DEFAULT_STAGE_WIDTH) / 2;
        double centerY = primaryScreenBounds.getMinY() + (screenHeight - DEFAULT_STAGE_HEIGHT) / 2;

        splashStage.setX(centerX);
        splashStage.setY(centerY);
    }

    /**
     * Applies the splash screen logo in the middle of container if it exists.
     *
     * @param splashLayout The container to apply the logo to.
     * @param logo         The logo path.
     */
    private void applyLogo(VBox splashLayout, String logo) {
        URL logoURL = SplashScreen.class.getResource(logo);
        if (logoURL != null) {
            Image logoImage = new Image(logoURL.toExternalForm(), 112, 112, true, true);
            ImageView logoImageView = new ImageView(logoImage);
            splashLayout.getChildren().add(logoImageView);
        } else {
            log.error("Logo not found: {}{}", ASSETS_DIR, LOGO_PATH);
        }
    }

    /**
     * Applies the splash screen stylesheet if it exists.
     *
     * @param scene      The scene to apply the stylesheet to.
     * @param stylesheet The stylesheet filename.
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
     * Sets the favicon for the splash stage if it exists.
     */
    private void applyFavicon() {
        URL faviconPath = getClass().getResource(FAVICON_PATH);
        if (faviconPath != null) {
            splashStage.getIcons().add(new javafx.scene.image.Image(faviconPath.toExternalForm()));
        } else {
            log.warn("Favicon not found: {}", FAVICON_PATH);
        }
    }

    /**
     * Shows the splash screen.
     */
    public void show() {
        splashStage.show();
    }

    /**
     * Closes the splash screen.
     */
    public void close() {
        splashStage.close();
    }
}