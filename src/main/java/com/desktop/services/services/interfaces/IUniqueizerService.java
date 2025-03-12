package com.desktop.services.services.interfaces;

import com.desktop.dto.UniqueizerRequestDTO;
import com.desktop.dto.UniqueizerResponseDTO;

import java.util.concurrent.CompletableFuture;

public interface IUniqueizerService {
    CompletableFuture<UniqueizerResponseDTO> UniqueizeWeb(UniqueizerRequestDTO uniqueizerRequest);
}
