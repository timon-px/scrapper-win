package com.desktop.services.driver;

import com.desktop.services.models.DriverSaveModel;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CapturePageWorker {
    private final List<DriverSaveModel> driverSaveModels = new ArrayList<>();
    private final boolean shouldProcessCss;

    public CapturePageWorker(boolean shouldProcessCss) {
        this.shouldProcessCss = shouldProcessCss;
    }

    public void CapturePage(String html, JavaScriptDriver javaScriptDriver) {
        if (!shouldProcessCss || Objects.isNull(javaScriptDriver)) {
            driverSaveModels.add(new DriverSaveModel(html));
            return;
        }

        try {
            String styles = javaScriptDriver.GetAllDocumentStyles();
            driverSaveModels.add(new DriverSaveModel(html, styles));
        } catch (Exception e) {
            driverSaveModels.add(new DriverSaveModel(html));
        }
    }

    public void CaptureFinalPage(String html, JavaScriptDriver javaScriptDriver) {
        if (!driverSaveModels.isEmpty() || Strings.isNullOrEmpty(html)) return;
        CapturePage(html, javaScriptDriver);
    }

    public List<DriverSaveModel> GetSaveModelList() {
        return driverSaveModels;
    }

    public int GetAmount() {
        return driverSaveModels.size();
    }

    public boolean ShouldProcessCss() {
        return shouldProcessCss;
    }
}
