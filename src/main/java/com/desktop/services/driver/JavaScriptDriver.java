package com.desktop.services.driver;

import com.desktop.services.config.constants.DriverConstants;
import com.desktop.services.utils.FilesWorker;
import org.openqa.selenium.JavascriptExecutor;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class JavaScriptDriver {
    private final String randomString;
    private final String scriptString;
    private final JavascriptExecutor js;

    public JavaScriptDriver(JavascriptExecutor js, String randomString) throws IOException {
        this.randomString = randomString;
        this.js = js;

        String scriptPath = String.format("/%s/%s", DriverConstants.DRIVER_RESOURCE_FOLDER, DriverConstants.DRIVER_CAPTURE_SCRIPT_FILE);

        URL scriptUrl = this.getClass().getResource(scriptPath);
        if (scriptUrl == null) scriptString = "";
        else scriptString = Files.readString(FilesWorker.GetFileFromURL(scriptUrl).toPath());
    }

    public void Execute() {
        js.executeScript(scriptString, randomString);
    }

    public boolean IsReadyToSave() {
        Object isReadyObject = js.executeScript("return window.isReadyToSave;");
        return isReadyObject instanceof Boolean && (Boolean) isReadyObject;
    }

    public void SetSavedHtml() {
        js.executeScript("setSavedHtml();");
    }
}
