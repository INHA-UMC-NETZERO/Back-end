package com.inhabada.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRequestDto(
        @NotNull(message = "수량은 필수 항목입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity,

        @NotNull(message = "수령 일정(Slot) 선택은 필수입니다")
        Long slotId
) {
}
