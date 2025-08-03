package com.p2p.app.service;
import com.p2p.app.model.FileMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final ConcurrentHashMap<String, FileMetadata> fileRegistry = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public FileService() {
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Schedule cleanup task every hour
        scheduler.scheduleAtFixedRate(this::cleanupExpiredFiles, 1, 1, TimeUnit.HOURS);
    }

    public synchronized String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (fileRegistry.containsKey(code));

        return code;
    }

    public FileMetadata uploadFile(MultipartFile file, String uploaderId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String code = generateUniqueCode();
        String fileName = file.getOriginalFilename();
        String filePath = UPLOAD_DIR + code + "_" + fileName;

        // Save file to disk
        Path path = Paths.get(filePath);
        Files.copy(file.getInputStream(), path);

        // Create metadata
        FileMetadata metadata = new FileMetadata(
                code, fileName, filePath, file.getSize(),
                file.getContentType(), uploaderId
        );

        // Store in registry
        fileRegistry.put(code, metadata);

        return metadata;
    }

    public FileMetadata getFileMetadata(String code) {
        FileMetadata metadata = fileRegistry.get(code);
        if (metadata != null && metadata.isExpired()) {
            removeFile(code);
            return null;
        }
        return metadata;
    }

    public byte[] downloadFile(String code) throws IOException {
        FileMetadata metadata = getFileMetadata(code);
        if (metadata == null || !metadata.isAvailable()) {
            return null;
        }

        Path path = Paths.get(metadata.getFilePath());
        if (!Files.exists(path)) {
            removeFile(code);
            return null;
        }

        return Files.readAllBytes(path);
    }

    public boolean removeFile(String code) {
        FileMetadata metadata = fileRegistry.remove(code);
        if (metadata != null) {
            try {
                Path path = Paths.get(metadata.getFilePath());
                Files.deleteIfExists(path);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void cleanupExpiredFiles() {
        fileRegistry.entrySet().removeIf(entry -> {
            FileMetadata metadata = entry.getValue();
            if (metadata.isExpired()) {
                try {
                    Path path = Paths.get(metadata.getFilePath());
                    Files.deleteIfExists(path);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    public int getActiveFileCount() {
        return fileRegistry.size();
    }
}