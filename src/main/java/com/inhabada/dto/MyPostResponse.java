package com.inhabada.dto;

import com.inhabada.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyPostResponse(
        Long postId,
        String title,
        @Schema(description = "카테고리 한글 label", example = "식품")
        String category,
        @Schema(description = "서브카테고리 한글 label. 기타는 null일 수 있습니다.", example = "과자")
        String subCategory,
        String location,
        String thumbnailUrl,
        Integer remainingQuantity,
        Integer totalQuantity,
        @Schema(description = "하위 카테고리 기준 1개당 탄소 저감량. 단위는 gram입니다.", example = "110")
        Long unitCarbonSavingGram,
        @Schema(description = "완료된 요청으로 게시글 작성자에게 적립된 탄소 저감량 합계. 단위는 gram입니다.", example = "550")
        Long completedCarbonSavingGram,
        @Schema(description = "게시글 상태 한글 label", example = "나눔중", allowableValues = {"나눔중", "예약중", "마감"})
        PostStatus status,
        boolean closed,
        LocalDateTime createdAt,
        List<MyPostRequestItem> requests
) {
}
