package com.desktop.application.utils;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Utility class for creating styled Alert dialogs with a consistent favicon.
 */
public final class AlertUtils {
    private static final Logger log = LoggerFactory.getLogger(AlertUtils.class);

    private static final String ASSETS_DIR = "/assets/";
    private static final String FAVICON_PATH = ASSETS_DIR + "icons/favicon.png";

    private AlertUtils() {
        // Private constructor for util
    }

    /**
     * Creates an Alert with the specified type, title, header, and content, applying the favicon and optional stylesheet.
     *
     * @param type    The type of alert (e.g., ERROR, INFORMATION).
     * @return The configured Alert.
     */
    public static Alert CreateAlert(Alert.AlertType type) {
        var alert = new Alert(type);
        applyFavicon(alert);
        return alert;
    }

    /**
     * Creates an Alert with the specified type, title, header, and content, applying the favicon and optional stylesheet.
     *
     * @param type    The type of alert (e.g., ERROR, INFORMATION).
     * @param title   The title of the alert.
     * @param header  The header text, or null for no header.
     * @param content The content text.
     * @return The configured Alert.
     */
    public static Alert CreateAlert(Alert.AlertType type, String title, String header, String content) {
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        applyFavicon(alert);

        return alert;
    }

    /**
     * Applies the favicon to the alert's stage if it exists.
     *
     * @param alert The alert to apply the favicon to.
     */
    private static void applyFavicon(Alert alert) {
        URL faviconPath = AlertUtils.class.getResource(FAVICON_PATH);
        if (faviconPath != null) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(faviconPath.toExternalForm()));
        } else {
            log.warn("Favicon not found: {}", FAVICON_PATH);
        }
    }
}