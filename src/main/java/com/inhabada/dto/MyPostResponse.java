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
        @Schema(description = "게시글 상태 한글 label", example = "나눔중", allowableValues = {"나눔중", "예약중", "마감"})
        PostStatus status,
        boolean closed,
        LocalDateTime createdAt,
        List<MyPostRequestItem> requests
) {
}
