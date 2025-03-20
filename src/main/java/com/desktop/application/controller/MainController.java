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
        FXMLLoader uniqueizerTab = new FXMLLoader(ScrapperApplication.class.getResource("uniqueizer-tab-view.fxml"));
        uniqueizer_tab.setContent(uniqueizerTab.load());
        uniqueizer_tab.setClosable(false);
    }
}
