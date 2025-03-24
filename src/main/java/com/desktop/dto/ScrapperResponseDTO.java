package com.desktop.dto;

import java.nio.file.Path;

public class ScrapperResponseDTO {
    private boolean success;
    private Path directory;
    private String message;

    public ScrapperResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ScrapperResponseDTO(boolean success, Path directory, String message) {
        this.success = success;
        this.directory = directory;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
