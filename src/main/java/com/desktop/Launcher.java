package com.desktop;

import com.desktop.application.ScrapperApplication;

public class Launcher {
    public static void main(String[] args) {
        // for proper font displaying
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        ScrapperApplication.main(args);
    }
}
