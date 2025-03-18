package com.desktop.dto;

import java.io.File;
import java.nio.file.Path;

public class UniqueizerRequestDTO {
    private File file;
    private Path savePath;
    private boolean isReplaceSelected;

    public UniqueizerRequestDTO(File file, Path savePath, boolean isReplaceSelected) {
        this.file = file;
        this.savePath = savePath;
        this.isReplaceSelected = isReplaceSelected;
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

    public boolean isReplaceSelected() {
        return isReplaceSelected;
    }

    public void setReplaceSelected(boolean replaceSelected) {
        isReplaceSelected = replaceSelected;
    }
}
