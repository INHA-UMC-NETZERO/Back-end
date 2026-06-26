package com.inhabada.dto;

import com.inhabada.entity.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MyPostResponse(
        Long postId,
        String title,
        String category,
        String thumbnailUrl,
        Integer remainingQuantity,
        Integer totalQuantity,
        PostStatus status,
        boolean closed,
        LocalDateTime createdAt,
        List<MyPostRequestItem> requests
) {
}
