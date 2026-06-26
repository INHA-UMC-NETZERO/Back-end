package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestDto(
        @NotNull(message = "수량은 필수 항목입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        @Schema(description = "신청 수량. 게시글의 남은 수량과 이미 신청중인 수량 합계를 기준으로 검증됩니다.", example = "1")
        Integer quantity,

        @NotBlank(message = "희망 수령 시간은 필수 항목입니다")
        @Size(max = 500, message = "희망 수령 시간은 500자 이하여야 합니다")
        @Schema(description = "희망 수령 시간", example = "금요일 오후 5시")
        String requestedTime
) {
}
