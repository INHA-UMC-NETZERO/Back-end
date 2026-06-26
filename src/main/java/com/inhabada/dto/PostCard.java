package com.inhabada.dto;

import com.inhabada.entity.PostStatus;

public record PostCard(
        Long id,
        String title,
        String thumbnailUrl,
        Integer remainingQuantity,
        String category,
        String categoryLabel,
        String subCategory,
        String subCategoryLabel,
        PostStatus status,
        boolean closed
) {
}
