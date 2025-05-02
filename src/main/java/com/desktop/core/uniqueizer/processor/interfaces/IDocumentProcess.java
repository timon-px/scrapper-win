package com.desktop.core.uniqueizer.processor.interfaces;

import javafx.beans.property.DoubleProperty;
import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public interface IDocumentProcess {
    CompletableFuture<Void> ProcessAsync(Document document, DoubleProperty progress);
}
