package com.inhabada.dto;

import com.inhabada.entity.KeywordSubscription;

import java.time.LocalDateTime;

public record KeywordResponse(
        String keyword,
        LocalDateTime createdAt
) {
    public static KeywordResponse from(KeywordSubscription subscription) {
        return new KeywordResponse(subscription.getKeyword(), subscription.getCreatedAt());
    }
}
