package com.inhabada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KeywordRequest(
        @NotBlank(message = "키워드는 필수 항목입니다")
        @Size(min = 1, max = 20, message = "키워드는 1자 이상 20자 이하여야 합니다")
        String keyword
) {
}
