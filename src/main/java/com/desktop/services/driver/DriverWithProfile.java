package com.desktop.services.driver;

import org.openqa.selenium.WebDriver;

import java.io.IOException;

public class DriverWithProfile implements AutoCloseable {
    private final WebDriver driver;
    private final TempProfileManager profileManager;

    public DriverWithProfile(WebDriver driver, TempProfileManager profileManager) {
        this.driver = driver;
        this.profileManager = profileManager;
    }

    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.quit();
        }
        if (profileManager != null) {
            profileManager.close();
        }
    }
}
