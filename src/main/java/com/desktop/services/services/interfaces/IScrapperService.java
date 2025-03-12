package com.desktop.services.services.interfaces;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.dto.ScrapperResponseDTO;
import javafx.beans.property.DoubleProperty;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface IScrapperService {
    DoubleProperty progressProperty();

    CompletableFuture<ScrapperResponseDTO> GetWeb(ScrapperRequestDTO request);
}
