package com.desktop.application.controller.worker.interfaces;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.List;

public interface IControllerWorker {
    void SetErrorLabel(Label label, String error);

    void SetLoading(boolean isLoading, List<Node> disableNodes, ProgressBar progressBar);
}
