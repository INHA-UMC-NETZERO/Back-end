package com.inhabada.service;

import com.inhabada.dto.PresignedUrlRequest;
import com.inhabada.dto.PresignedUrlResponse;
import com.inhabada.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class UploadService {

    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int expirationMinutes;

    public UploadService(S3Presigner s3Presigner,
                         @Value("${aws.s3.bucket-name}") String bucketName,
                         @Value("${aws.s3.presigned-url-expiration-minutes:10}") int expirationMinutes) {
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.expirationMinutes = expirationMinutes;
    }

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        if (!request.contentType().startsWith("image/")) {
            throw new ValidationException("이미지 파일만 업로드할 수 있습니다");
        }

        String key = buildKey(request.fileName());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(presigned.url().toString(), key, expirationMinutes);
    }

    private String buildKey(String fileName) {
        String ext = extractExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        // 게시글 생성 전 발급되므로 postId 없이 posts/{uuid}.{ext} 형식 사용
        return ext.isEmpty() ? "posts/" + uuid : "posts/" + uuid + "." + ext;
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
