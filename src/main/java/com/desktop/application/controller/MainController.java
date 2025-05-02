package com.desktop.application.controller;

import com.desktop.application.ScrapperApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class MainController {
    @FXML
    private TabPane tab_pane;

    @FXML
    private Tab uniqueizer_tab;

    @FXML
    private void initialize() throws IOException {
        loadTabContent(uniqueizer_tab, "uniqueizer-tab-view.fxml");
        uniqueizer_tab.setClosable(false);
    }

    /**
     * Loads content for a tab from the specified FXML file.
     *
     * @param tab      The tab to set content for.
     * @param fxmlPath The path to the FXML file.
     * @throws IOException If the FXML file cannot be loaded.
     */
    private void loadTabContent(Tab tab, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(ScrapperApplication.class.getResource(fxmlPath));
        tab.setContent(loader.load());
    }
}
