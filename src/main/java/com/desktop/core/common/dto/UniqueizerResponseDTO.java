package com.desktop.core.common.dto;

import java.nio.file.Path;
import java.util.List;

public class UniqueizerResponseDTO {
    private boolean success;
    private List<Path> directories;
    private String message;

    public UniqueizerResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public UniqueizerResponseDTO(boolean success, List<Path> directories, String message) {
        this.success = success;
        this.directories = directories;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Path> getDirectories() {
        return directories;
    }

    public void setDirectories(List<Path> directories) {
        this.directories = directories;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
