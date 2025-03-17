package com.desktop.services.services.interfaces;

import com.desktop.dto.UniqueizerRequestDTO;
import com.desktop.dto.UniqueizerResponseDTO;
import javafx.beans.property.DoubleProperty;

import java.util.concurrent.CompletableFuture;

public interface IUniqueizerService {
    DoubleProperty progressProperty();

    CompletableFuture<UniqueizerResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest);
}
