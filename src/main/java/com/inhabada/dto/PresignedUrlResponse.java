package com.inhabada.dto;

public record PresignedUrlResponse(
        String uploadUrl,
        String key,
        int expiresInMinutes
) {
}
