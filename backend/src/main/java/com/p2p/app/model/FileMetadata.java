package com.p2p.app.model;

import java.time.LocalDateTime;

public class FileMetadata {
    private String code;
    private String fileName;
    private String filePath;
    private long fileSize;
    private String contentType;
    private LocalDateTime uploadTime;
    private LocalDateTime expiryTime;
    private String uploaderId;
    private boolean isAvailable;

    public FileMetadata() {}

    public FileMetadata(String code, String fileName, String filePath, long fileSize,
                        String contentType, String uploaderId) {
        this.code = code;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploaderId = uploaderId;
        this.uploadTime = LocalDateTime.now();
        this.expiryTime = LocalDateTime.now().plusHours(24); // 24 hour expiry
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}