package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageCarbonSummary(
        @Schema(description = "사용자의 누적 탄소 저감량. 단위는 gram입니다.", example = "6700")
        Long totalCarbonSavingGram
) {
}
