package com.desktop.core.driver;

import com.desktop.core.common.model.DriverSaveModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CapturePageWorker {
    private final List<DriverSaveModel> driverSaveModels = new ArrayList<>();
    private final boolean shouldProcessCss;

    private DriverSaveModel tempDriverSaveModel;

    public CapturePageWorker(boolean shouldProcessCss) {
        this.shouldProcessCss = shouldProcessCss;
    }

    public void CapturePage(String html, JavaScriptDriver javaScriptDriver, boolean shouldSave) {
        if (!shouldSave) tempDriverSaveModel = new DriverSaveModel(html);
        else CapturePage(html, javaScriptDriver);
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

    public void CaptureFinalPage() {
        if (!driverSaveModels.isEmpty()) return;
        driverSaveModels.add(tempDriverSaveModel);
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
