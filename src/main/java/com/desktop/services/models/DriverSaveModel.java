package com.desktop.services.models;

public class DriverSaveModel {
    private String html;
    private String style;

    public DriverSaveModel(String html) {
        this.html = html;
        this.style = null;
    }

    public DriverSaveModel(String html, String style) {
        this.html = html;
        this.style = style;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
