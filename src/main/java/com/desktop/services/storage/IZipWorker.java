package com.desktop.services.storage;

import java.io.IOException;

public interface IZipWorker {
    void Init();

    String CreateZipArchive(String sourceFolderPath, String zipFileName) throws IOException;
}
