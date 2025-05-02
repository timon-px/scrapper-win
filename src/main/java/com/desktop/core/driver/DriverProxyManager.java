package com.desktop.core.driver;

import com.desktop.core.driver.interfaces.IDriverOptionsManager;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverProxyManager implements IDriverOptionsManager {
    private final String proxy;

    public DriverProxyManager(String proxy) {
        this.proxy = proxy;
    }

    public DriverProxyManager(String proxyHost, int proxyPort) {
        this.proxy = String.format("%s:%s", proxyHost, proxyPort);
    }

    @Override
    public void ApplyToOptions(ChromeOptions options) {
        if (proxy == null) return;

        Proxy chromeProxy = new Proxy();
        chromeProxy.setHttpProxy(proxy);
        chromeProxy.setSslProxy(proxy);

        options.setProxy(chromeProxy);
    }
}
