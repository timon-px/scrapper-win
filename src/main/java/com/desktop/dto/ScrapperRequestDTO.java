package com.desktop.dto;

import java.nio.file.Path;

public class ScrapperRequestDTO {
    private Path directory;
    private String url;
    private boolean isReplaceSelected;

    public ScrapperRequestDTO(Path directory, String url, boolean isReplaceSelected) {
        this.directory = directory;
        this.url = url;
        this.isReplaceSelected = isReplaceSelected;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isReplaceSelected() {
        return isReplaceSelected;
    }

    public void setReplaceSelected(boolean replaceSelected) {
        isReplaceSelected = replaceSelected;
    }
}
