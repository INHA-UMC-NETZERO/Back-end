package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CarbonRankingResponse(
        @Schema(description = "랭킹 기준 월. yyyy-MM 형식입니다.", example = "2026-06")
        String yearMonth,
        @Schema(description = "월간 탄소 저감량 랭킹 목록")
        List<CarbonRankingItem> items
) {
}
