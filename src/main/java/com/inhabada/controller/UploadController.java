package com.inhabada.controller;

import com.inhabada.dto.PresignedUrlRequest;
import com.inhabada.dto.PresignedUrlResponse;
import com.inhabada.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(uploadService.generatePresignedUrl(request));
    }
}
