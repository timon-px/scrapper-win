package com.desktop.core.driver;

import com.desktop.core.common.constants.DriverConstants;
import com.desktop.core.utils.ResourceExtractor;
import com.google.common.base.Strings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JavaScriptDriver {
    private static final Logger log = LoggerFactory.getLogger(JavaScriptDriver.class);
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

    public void SetLoadedHtml() {
        initEvent("loadedHtmlEvent", null);
    }

    public void SetSavedHtml(int amount) {
        initEvent("savedHtmlEvent", String.valueOf(amount));
    }

    public void UpdateAmountCaptures(int amount) {
        initEvent("updateAmountHtmlEvent", String.valueOf(amount));
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
