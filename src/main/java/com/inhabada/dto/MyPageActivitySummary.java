package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageActivitySummary(
        @Schema(description = "내가 등록한 게시글에서 완료된 나눔 요청 수", example = "3")
        Long sharedCount,
        @Schema(description = "내가 신청해서 완료된 수령 요청 수", example = "2")
        Long receivedCount,
        @Schema(description = "내가 기부자 또는 수령자로 포함된 완료 요청 수", example = "5")
        Long completedDeliveryCount,
        @Schema(description = "내가 기부자 또는 수령자로 포함된 전체 요청 대비 완료 요청 비율. 단위는 percent입니다.", example = "63")
        Integer deliveryCompletionRate
) {
}
