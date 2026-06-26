package com.inhabada.dto;

import com.inhabada.entity.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String description,
        String category,
        String categoryLabel,
        String subCategory,
        String subCategoryLabel,
        List<String> imageUrls,
        Integer remainingQuantity,
        Integer totalQuantity,
        String location,
        Long giverId,
        String giverName,
        PostStatus status,
        boolean closed,
        LocalDateTime createdAt,
        String availableTime
) {
}
