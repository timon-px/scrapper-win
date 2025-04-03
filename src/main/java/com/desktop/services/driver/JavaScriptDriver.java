package com.desktop.services.driver;

import com.desktop.services.config.constants.DriverConstants;
import com.desktop.services.utils.FilesWorker;
import com.google.common.base.Strings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URL;

public class JavaScriptDriver {
    private final JavascriptExecutor js;

    public JavaScriptDriver(JavascriptExecutor js) {
        this.js = js;
    }

    public static ChromeOptions GetChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        URL scriptUrl = JavaScriptDriver.class.getResource(DriverConstants.DRIVER_EXTENSION_PATH);
        if (scriptUrl != null) options.addExtensions(FilesWorker.GetFileFromURL(scriptUrl));

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
        else eventJs = String.format("document.dispatchEvent(new CustomEvent(\"%s\", { detail: %s }))", event, argument);

        js.executeScript(eventJs);
    }
}
