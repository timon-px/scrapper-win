package com.desktop.services.services.interfaces;

import com.desktop.dto.ScrapperResponseDTO;
import com.desktop.dto.UniqueizerRequestDTO;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface IUniqueizerService {
    CompletableFuture<ScrapperResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest);
}
