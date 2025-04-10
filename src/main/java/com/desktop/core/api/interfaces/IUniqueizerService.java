package com.desktop.core.api.interfaces;

import com.desktop.core.common.dto.UniqueizerRequestDTO;
import com.desktop.core.common.dto.UniqueizerResponseDTO;
import javafx.beans.property.DoubleProperty;

import java.util.concurrent.CompletableFuture;

public interface IUniqueizerService {
    DoubleProperty progressProperty();

    CompletableFuture<UniqueizerResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest);
}
