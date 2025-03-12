package com.desktop.services.processors.interfaces;

import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public interface IScrapperProcess {
    CompletableFuture<Void> ProcessAsync(Document document);
}
