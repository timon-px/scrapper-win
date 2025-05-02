package com.desktop.core.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * A versatile class for extracting resources (files or directories) from JAR or classpath into a temporary or specified directory.
 */
public class ResourceExtractor {
    private static final Logger log = LoggerFactory.getLogger(ResourceExtractor.class);

    private static final int BUFFER_SIZE = 8192; // 8KB buffer for efficient copying

    /**
     * Resolves a resource (file) to a File object, either directly (development) or by extracting from JAR (deployment).
     *
     * @param resourcePath Path to the resource (e.g., "/files/my-file.txt")
     * @param deleteOnExit Whether to delete the extracted file on exit (applies only if extracted)
     * @return A File object representing the resource
     * @throws IOException If the resource cannot be resolved
     */
    public static File ResolveResource(String resourcePath, boolean deleteOnExit) throws IOException {
        Objects.requireNonNull(resourcePath, "Resource path cannot be null");

        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;

        URL resourceUrl = ResourceExtractor.class.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IOException("Resource not found at path: " + resourcePath);
        }

        return resolveUrlToFile(resourceUrl, resourcePath, deleteOnExit);
    }

    /**
     * Extracts a resource to a specific directory, either directly or by extracting.
     *
     * @param resourcePath Path to the resource
     * @param targetDir    Target directory
     * @param fileName     Name of the file in the target directory
     * @return A File object representing the resource
     * @throws IOException If the resource cannot be resolved
     */
    public static File ExtractResource(String resourcePath, Path targetDir, String fileName) throws IOException {
        Objects.requireNonNull(resourcePath, "Resource path cannot be null");
        Objects.requireNonNull(targetDir, "Target directory cannot be null");
        Objects.requireNonNull(fileName, "File name cannot be null");

        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;

        URL resourceUrl = ResourceExtractor.class.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IOException("Resource not found at path: " + resourcePath);
        }

        Files.createDirectories(targetDir);
        File targetFile = targetDir.resolve(fileName).toFile();

        if (resourceUrl.getProtocol().equals("file")) {
            // If it's a file in the filesystem, copy it to the target directory
            Files.copy(Path.of(resourceUrl.getPath()), targetFile.toPath());
        } else {
            // Extract from JAR
            try (InputStream inputStream = resourceUrl.openStream();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                copyStream(inputStream, outputStream);
            }
        }

        return targetFile;
    }

    /**
     * Resolves a directory from the classpath or JAR.
     *
     * @param directoryPath Path to the directory (e.g., "/myfolder/")
     * @throws IOException If an error occurs during resolution
     */
    public static Path ResolveDirectory(String directoryPath, boolean deleteOnExit) throws IOException {
        Objects.requireNonNull(directoryPath, "Directory path cannot be null");

        String normalizedDirPath = getNormalizedDirPath(directoryPath);

        URL dirUrl = ResourceExtractor.class.getResource("/" + normalizedDirPath);
        if (dirUrl == null) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        return resolveDirUrlToPath(dirUrl, normalizedDirPath, deleteOnExit);
    }

    /**
     * Extracts a directory from the classpath or JAR to a target directory.
     *
     * @param directoryPath Path to the directory (e.g., "/myfolder/")
     * @param targetDir     Target directory on the filesystem
     * @throws IOException If an error occurs during resolution
     */
    public static void ExtractDirectory(String directoryPath, Path targetDir) throws IOException {
        Objects.requireNonNull(directoryPath, "Directory path cannot be null");
        Objects.requireNonNull(targetDir, "Target directory cannot be null");

        String normalizedDirPath = getNormalizedDirPath(directoryPath);

        URL dirUrl = ResourceExtractor.class.getResource("/" + normalizedDirPath);
        if (dirUrl == null) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        if (dirUrl.getProtocol().equals("file")) {
            // Development mode: copy directory from filesystem
            Path sourceDir = getPathFromURL(dirUrl);
            copyDirectory(sourceDir, targetDir);
        } else if (dirUrl.getProtocol().equals("jar")) {
            // Deployment mode: extract from JAR
            String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
            extractDirectoryFromJar(jarPath, normalizedDirPath, targetDir);
        } else {
            throw new UnsupportedOperationException("Unsupported protocol: " + dirUrl.getProtocol());
        }
    }

    /**
     * Helper method to resolve a URL to a File object.
     */
    private static File resolveUrlToFile(URL resourceUrl, String resourcePath, boolean deleteOnExit) throws IOException {
        if (resourceUrl.getProtocol().equals("file")) {
            // Development mode: return the file directly from the filesystem
            return getPathFromURL(resourceUrl).toFile();
        } else if (resourceUrl.getProtocol().equals("jar")) {
            // Deployment mode: extract from JAR
            File tempFile = File.createTempFile("scrapper-resource", getFileExtension(resourcePath));
            if (deleteOnExit) {
                tempFile.deleteOnExit();
            }
            try (InputStream inputStream = resourceUrl.openStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                copyStream(inputStream, outputStream);
            }
            return tempFile;
        } else {
            throw new UnsupportedOperationException("Unsupported protocol: " + resourceUrl.getProtocol());
        }
    }

    /**
     * Helper method to resolve a directory URL to a Path object.
     */
    private static Path resolveDirUrlToPath(URL dirUrl, String resourcePath, boolean deleteOnExit) throws IOException {
        if (dirUrl.getProtocol().equals("file")) {
            // Development mode: return the directory directly from the filesystem
            return getPathFromURL(dirUrl);
        } else if (dirUrl.getProtocol().equals("jar")) {
            // Deployment mode: extract from JAR
            Path targetDir = Files.createTempDirectory("scrapper-resource-dir");

            // Deployment mode: extract from JAR
            String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
            extractDirectoryFromJar(jarPath, resourcePath, targetDir);

            setRemoveOnExitDirectories(targetDir, deleteOnExit);

            return targetDir;
        } else {
            throw new UnsupportedOperationException("Unsupported protocol: " + dirUrl.getProtocol());
        }
    }

    /**
     * Extracts a directory from a JAR file.
     */
    private static void extractDirectoryFromJar(String jarPath, String directoryPath, Path targetDir) throws IOException {
        String encodedPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        try (JarFile jarFile = new JarFile(encodedPath)) {
            jarFile.stream()
                    .filter(entry -> entry.getName().startsWith(directoryPath) && !entry.isDirectory())
                    .forEach(entry -> {
                        try {
                            extractJarEntry(jarFile, entry, targetDir, directoryPath);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to extract entry: " + entry.getName(), e);
                        }
                    });
        }
    }

    /**
     * Copies a directory from the filesystem to a target directory.
     */
    private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        try (Stream<Path> walk = Files.walk(sourceDir)) {
            walk.forEach(source -> {
                Path target = targetDir.resolve(sourceDir.relativize(source));
                try {
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy: " + source, e);
                }
            });
        }
    }

    /**
     * Extracts a single JAR entry to the target directory.
     */
    private static void extractJarEntry(JarFile jarFile, JarEntry entry, Path targetDir, String basePath) throws IOException {
        Path targetPath = targetDir.resolve(entry.getName().substring(basePath.length()));
        Files.createDirectories(targetPath.getParent());

        try (InputStream inputStream = jarFile.getInputStream(entry);
             FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
            copyStream(inputStream, outputStream);
        }
    }

    /**
     * Efficiently copies data from an input stream to an output stream.
     */
    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Extracts the file extension from a resource path.
     */
    private static String getFileExtension(String resourcePath) {
        int lastDot = resourcePath.lastIndexOf('.');
        return (lastDot == -1) ? "" : resourcePath.substring(lastDot);
    }

    /**
     * Remove folder after closing app.
     */
    private static void setRemoveOnExitDirectories(Path directory, boolean removeOnExit) {
        if (!removeOnExit) return;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(directory.toFile());
            } catch (IOException ex) {
                log.error("Failed to delete temporary directory", ex);
            }
        }));
    }

    /**
     * Getting normalized directory path from resource path.
     */
    private static String getNormalizedDirPath(String directoryPath) {
        String normalizedDirPath = directoryPath.startsWith("/") ? directoryPath.substring(1) : directoryPath;
        if (!normalizedDirPath.endsWith("/")) {
            normalizedDirPath += "/";
        }
        return normalizedDirPath;
    }

    /**
     * Getting correct path from url.
     */
    public static Path getPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }
}