package com.inhabada.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SlotRequest(
        @NotNull(message = "시작 시각은 필수 항목입니다")
        LocalDateTime startTime,

        @NotNull(message = "종료 시각은 필수 항목입니다")
        LocalDateTime endTime
) {
}
