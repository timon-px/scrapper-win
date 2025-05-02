package com.desktop.core.common.model;

public class UniqueizeHtmlEntry {
    private String key;
    private String value;

    public UniqueizeHtmlEntry() {

    }

    public UniqueizeHtmlEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public UniqueizeHtmlEntry(int key, String value) {
        this.key = String.valueOf(key);
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
