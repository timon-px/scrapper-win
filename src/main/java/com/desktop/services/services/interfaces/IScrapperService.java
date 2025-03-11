package com.desktop.services.services.interfaces;

import com.desktop.dto.ScrapperRequestDTO;
import com.desktop.dto.ScrapperResponseDTO;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface IScrapperService {

    CompletableFuture<ScrapperResponseDTO> GetWeb(ScrapperRequestDTO request);
}
