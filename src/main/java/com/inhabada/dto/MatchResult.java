package com.inhabada.dto;

public record MatchResult(
        Long postId,
        String title,
        String imageUrl,
        Integer remainingQuantity,
        String webviewLink,
        double similarity
) {
}
