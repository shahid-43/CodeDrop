package com.p2p.app.controller;
import com.p2p.app.model.FileMetadata;
import com.p2p.app.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private WebSocketController webSocketController;

    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> response = new HashMap<>();
            String sessionId = UUID.randomUUID().toString();

            try {
                // Simulate progress updates
                webSocketController.notifyUploadProgress(sessionId, 25);
                Thread.sleep(500);
                webSocketController.notifyUploadProgress(sessionId, 50);
                Thread.sleep(500);
                webSocketController.notifyUploadProgress(sessionId, 75);
                Thread.sleep(500);

                FileMetadata metadata = fileService.uploadFile(file, sessionId);

                webSocketController.notifyUploadProgress(sessionId, 100);
                webSocketController.notifyUploadComplete(sessionId, metadata.getCode(), metadata.getFileName());

                response.put("success", true);
                response.put("code", metadata.getCode());
                response.put("fileName", metadata.getFileName());
                response.put("fileSize", metadata.getFileSize());
                response.put("sessionId", sessionId);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Upload failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        });
    }

    @GetMapping("/download/{code}")
    public CompletableFuture<ResponseEntity<byte[]>> downloadFile(
            @PathVariable String code,
            HttpServletRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String sessionId = UUID.randomUUID().toString();

            try {
                FileMetadata metadata = fileService.getFileMetadata(code);
                if (metadata == null) {
                    return ResponseEntity.notFound().build();
                }

                // Simulate download progress
                webSocketController.notifyDownloadProgress(sessionId, 25);
                Thread.sleep(200);
                webSocketController.notifyDownloadProgress(sessionId, 50);
                Thread.sleep(200);
                webSocketController.notifyDownloadProgress(sessionId, 75);
                Thread.sleep(200);

                byte[] fileData = fileService.downloadFile(code);
                if (fileData == null) {
                    return ResponseEntity.notFound().build();
                }

                webSocketController.notifyDownloadProgress(sessionId, 100);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
                headers.setContentDispositionFormData("attachment", metadata.getFileName());
                headers.setContentLength(fileData.length);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(fileData);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        });
    }

    @GetMapping("/file/{code}/info")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            FileMetadata metadata = fileService.getFileMetadata(code);
            if (metadata != null) {
                response.put("success", true);
                response.put("fileName", metadata.getFileName());
                response.put("fileSize", metadata.getFileSize());
                response.put("contentType", metadata.getContentType());
                response.put("uploadTime", metadata.getUploadTime().toString());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "File not found or expired");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving file info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/file/{code}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = fileService.removeFile(code);
            if (deleted) {
                response.put("success", true);
                response.put("message", "File deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "File not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}