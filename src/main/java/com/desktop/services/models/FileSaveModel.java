package com.desktop.services.models;

import com.desktop.services.config.enums.SaveAsEnum;

public class FileSaveModel {
    private String uniqueName;
    private SaveAsEnum fileType;

    public FileSaveModel(String uniqueName, SaveAsEnum fileType) {
        this.uniqueName = uniqueName;
        this.fileType = fileType;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public SaveAsEnum getFileType() {
        return fileType;
    }

    public void setFileType(SaveAsEnum fileType) {
        this.fileType = fileType;
    }
}
