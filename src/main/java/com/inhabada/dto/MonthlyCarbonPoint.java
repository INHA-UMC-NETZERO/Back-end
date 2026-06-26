package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MonthlyCarbonPoint(
        @Schema(description = "월. yyyy-MM 형식입니다.", example = "2026-06")
        String month,
        @Schema(description = "해당 월의 탄소 저감량. 단위는 gram입니다.", example = "1200")
        Long carbonSavingGram
) {
}
