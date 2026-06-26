package com.inhabada.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestDto(
        @NotNull(message = "수량은 필수 항목입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity,

        @NotBlank(message = "희망 수령 시간은 필수 항목입니다")
        @Size(max = 500, message = "희망 수령 시간은 500자 이하여야 합니다")
        String requestedTime
) {
}
