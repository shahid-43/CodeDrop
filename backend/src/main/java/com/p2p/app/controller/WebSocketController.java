package com.p2p.app.controller;

import com.p2p.app.model.FileMetadata;
import com.p2p.app.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private FileService fileService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/file/check")
    @SendTo("/topic/file/status")
    public Map<String, Object> checkFileStatus(Map<String, String> request) {
        String code = request.get("code");
        Map<String, Object> response = new HashMap<>();

        try {
            FileMetadata metadata = fileService.getFileMetadata(code);
            if (metadata != null) {
                response.put("success", true);
                response.put("fileName", metadata.getFileName());
                response.put("fileSize", metadata.getFileSize());
                response.put("contentType", metadata.getContentType());
                response.put("uploadTime", metadata.getUploadTime().toString());
            } else {
                response.put("success", false);
                response.put("message", "File not found or expired");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking file: " + e.getMessage());
        }

        return response;
    }

    @MessageMapping("/system/stats")
    @SendTo("/topic/system/stats")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeFiles", fileService.getActiveFileCount());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    public void notifyUploadProgress(String sessionId, int progress) {
        Map<String, Object> message = new HashMap<>();
        message.put("progress", progress);
        message.put("status", "uploading");
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/upload/progress", message);
    }

    public void notifyUploadComplete(String sessionId, String code, String fileName) {
        Map<String, Object> message = new HashMap<>();
        message.put("success", true);
        message.put("code", code);
        message.put("fileName", fileName);
        message.put("status", "completed");
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/upload/complete", message);
    }

    public void notifyDownloadProgress(String sessionId, int progress) {
        Map<String, Object> message = new HashMap<>();
        message.put("progress", progress);
        message.put("status", "downloading");
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/download/progress", message);
    }
}
