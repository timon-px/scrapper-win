package com.desktop.services.driver;

import com.desktop.services.config.constants.DriverConstants;
import com.desktop.services.utils.ResourceExtractor;
import com.google.common.base.Strings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;

public class JavaScriptDriver {
    private final JavascriptExecutor js;

    public JavaScriptDriver(JavascriptExecutor js) {
        this.js = js;
    }

    public static ChromeOptions GetChromeOptions() throws IOException {
        ChromeOptions options = new ChromeOptions();

        File extensionFile = ResourceExtractor.ResolveResource(DriverConstants.DRIVER_EXTENSION_PATH, true);
        options.addExtensions(extensionFile);

        return options;
    }

    public boolean IsReadyToSave() {
        Object isReadyObject = js.executeScript("return window.isReady_scrapper;");
        return isReadyObject instanceof Boolean && (Boolean) isReadyObject;
    }

    public void SetSavedHtml(int amount) {
        initEvent("savedHtmlEvent", String.valueOf(amount));
    }

    public String GetAllDocumentStyles() {
        Object allStylesObject = js.executeScript("return window.getStylesheetsString_scrapper();");
        if (allStylesObject instanceof String) return (String) allStylesObject;

        throw new IllegalAccessError("All styles object is not a string");
    }

    private void initEvent(String event, String argument) {
        String eventJs;

        if (Strings.isNullOrEmpty(argument))
            eventJs = String.format("document.dispatchEvent(new CustomEvent(\"%s\"))", event);
        else
            eventJs = String.format("document.dispatchEvent(new CustomEvent(\"%s\", { detail: %s }))", event, argument);

        js.executeScript(eventJs);
    }
}
