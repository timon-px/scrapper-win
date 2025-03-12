package com.desktop.dto;

import java.io.File;
import java.nio.file.Path;

public class UniqueizerResponseDTO {
    private boolean success;
    private String message;

    public UniqueizerResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
