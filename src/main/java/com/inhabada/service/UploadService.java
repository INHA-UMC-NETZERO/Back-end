package com.inhabada.service;

import com.inhabada.dto.UploadResponse;
import com.inhabada.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class UploadService {

    private final S3Client s3Client;
    private final ImageUrlResolver imageUrlResolver;
    private final String bucketName;

    public UploadService(S3Client s3Client,
                         ImageUrlResolver imageUrlResolver,
                         @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.imageUrlResolver = imageUrlResolver;
        this.bucketName = bucketName;
    }

    public UploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("업로드할 파일이 없습니다");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("이미지 파일만 업로드할 수 있습니다");
        }

        String key = buildKey(file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다", e);
        }

        return new UploadResponse(key, imageUrlResolver.toUrl(key));
    }

    private String buildKey(String fileName) {
        String ext = extractExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        // 게시글 생성 전 발급되므로 postId 없이 posts/{uuid}.{ext} 형식 사용
        return ext.isEmpty() ? "posts/" + uuid : "posts/" + uuid + "." + ext;
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
