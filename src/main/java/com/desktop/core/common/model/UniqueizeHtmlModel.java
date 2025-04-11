package com.desktop.core.common.model;

import java.util.List;

public class UniqueizeHtmlModel {
    private List<UniqueizeHtmlEntry> html;
    private String lang;

    public UniqueizeHtmlModel() {

    }

    public UniqueizeHtmlModel(List<UniqueizeHtmlEntry> html, String lang) {
        this.html = html;
        this.lang = lang;
    }

    public List<UniqueizeHtmlEntry> getHtml() {
        return html;
    }

    public void setHtml(List<UniqueizeHtmlEntry> html) {
        this.html = html;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
