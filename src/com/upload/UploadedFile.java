package com.upload;

import java.io.File;
import java.io.InputStream;

public class UploadedFile {
    private String fileName;
    private String contentType;
    private long size;
    private InputStream inputStream;
    private String savedPath;

    public UploadedFile(String fileName, String contentType, long size, InputStream inputStream) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getSavedPath() {
        return savedPath;
    }

    public void setSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }

    public String getExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    public boolean save(String directory) {
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, fileName);
            java.nio.file.Files.copy(inputStream, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            savedPath = file.getAbsolutePath();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 