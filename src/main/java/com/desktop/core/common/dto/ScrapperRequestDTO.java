package com.desktop.core.common.dto;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;

public class ScrapperRequestDTO {
    private final Path directory;
    private final String url;
    private final ProcessingOptions processingOptions;

    public ScrapperRequestDTO(Path directory, String url, ProcessingOptions processingOptions) {
        this.directory = directory;
        this.url = url;
        this.processingOptions = processingOptions;
    }

    public Path getDirectory() {
        return directory;
    }


    public String getUrl() {
        return url;
    }

    public ProcessingOptions getProcessingOptions() {
        return processingOptions;
    }

    public static class ProcessingOptions {
        private final boolean replaceHref;
        private final boolean processDriver;
        private final boolean processDriverCustomStyles;
        private final String proxyHost;
        private final int proxyPort;

        public ProcessingOptions(boolean replaceHref, boolean processDriver, boolean processDriverCustomStyles) {
            this(replaceHref, processDriver, processDriverCustomStyles, null, -1);
        }

        public ProcessingOptions(boolean replaceHref, boolean processDriver, boolean processDriverCustomStyles, String proxyHost, int proxyPort) {
            this.replaceHref = replaceHref;
            this.processDriver = processDriver;
            this.processDriverCustomStyles = processDriverCustomStyles;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }

        public boolean shouldReplaceHref() {
            return replaceHref;
        }

        public boolean shouldProcessDriver() {
            return processDriver;
        }

        public boolean shouldProcessDriverCustomStyles() {
            return processDriverCustomStyles;
        }

        public Proxy getProxy() {
            try {
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            } catch (Exception e) {
                return Proxy.NO_PROXY;
            }
        }

        public String getProxyString() {
            if (proxyHost == null || proxyPort < 0)
                return null;

            return proxyHost + ":" + proxyPort;
        }
    }
}
