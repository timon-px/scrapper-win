package com.desktop.application.controller.worker;

import com.desktop.application.controller.worker.interfaces.IControllerWorker;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScrapperControllerWorker implements IControllerWorker {
    private static final Logger log = LoggerFactory.getLogger(ScrapperControllerWorker.class);

    @Override
    public void SetErrorLabel(Label label, String error) {
        label.setText("");
        if (error != null) label.setText(error);
    }

    @Override
    public void SetLoading(boolean isLoading, List<Node> disableNodes, ProgressBar progressBar) {
        for (Node nodes : disableNodes) {
            nodes.setDisable(isLoading);
        }

        progressBar.setVisible(isLoading);
    }
}
