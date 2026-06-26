package com.inhabada.dto;

import jakarta.validation.constraints.NotBlank;

public record SemanticMatchRequest(
        @NotBlank(message = "상품명은 필수 항목입니다")
        String productName
) {
}
