package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CarbonRankingItem(
        @Schema(description = "랭킹 순위. 동점도 단순 순번으로 반환합니다.", example = "1")
        int rank,
        @Schema(description = "사용자 ID", example = "10")
        Long userId,
        @Schema(description = "사용자 닉네임", example = "인하대 학생회")
        String nickname,
        @Schema(description = "이번 달 탄소 저감량. 단위는 gram입니다.", example = "45200")
        Long carbonSavingGram
) {
}
