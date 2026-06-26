package com.inhabada.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
        @NotBlank(message = "파일명은 필수 항목입니다")
        String fileName,

        @NotBlank(message = "Content-Type은 필수 항목입니다")
        String contentType
) {
}
