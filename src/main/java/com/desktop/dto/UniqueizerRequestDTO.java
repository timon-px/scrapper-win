package com.desktop.dto;

import java.io.File;
import java.nio.file.Path;

public class UniqueizerRequestDTO {
    private File file;
    private Path savePath;

    public UniqueizerRequestDTO(File file, Path savePath) {
        this.file = file;
        this.savePath = savePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Path getSavePath() {
        return savePath;
    }

    public void setSavePath(Path savePath) {
        this.savePath = savePath;
    }
}
