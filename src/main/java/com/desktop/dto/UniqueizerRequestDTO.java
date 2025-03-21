package com.desktop.dto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class UniqueizerRequestDTO {
    private final List<File> files;
    private final Path savePath;
    private final boolean isReplaceSelected;

    public UniqueizerRequestDTO(List<File> files, Path savePath, boolean isReplaceSelected) {
        this.files = files;
        this.savePath = savePath;
        this.isReplaceSelected = isReplaceSelected;
    }

    public List<File> getFiles() {
        return files;
    }

    public Path getSavePath() {
        return savePath;
    }

    public boolean isReplaceSelected() {
        return isReplaceSelected;
    }
}
