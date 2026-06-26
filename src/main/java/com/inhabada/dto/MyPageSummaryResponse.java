package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MyPageSummaryResponse(
        @Schema(description = "마이페이지 프로필 정보")
        MyPageProfileSummary profile,
        @Schema(description = "나눔/수령 활동 요약")
        MyPageActivitySummary activity,
        @Schema(description = "누적 탄소 저감량 요약")
        MyPageCarbonSummary carbon,
        @Schema(description = "최근 6개월 월별 탄소 저감량. 없는 달은 0으로 반환됩니다.")
        List<MonthlyCarbonPoint> monthlyCarbon
) {
}
