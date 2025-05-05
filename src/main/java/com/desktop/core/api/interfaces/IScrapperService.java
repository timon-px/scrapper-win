package com.desktop.core.api.interfaces;

import com.desktop.core.common.dto.ScrapperRequestDTO;
import com.desktop.core.common.dto.ScrapperResponseDTO;
import javafx.beans.property.DoubleProperty;

import java.util.concurrent.CompletableFuture;

public interface IScrapperService {
    DoubleProperty progressProperty();

    CompletableFuture<ScrapperResponseDTO> GetWeb(ScrapperRequestDTO request);
}
