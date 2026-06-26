package com.inhabada.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostStatus {
    ACTIVE("나눔중"),
    PENDING("예약중"),
    CLOSED("마감");

    private final String label;

    PostStatus(String label) {
        this.label = label;
    }

    @JsonCreator
    public static PostStatus from(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        for (PostStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized) || status.label.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 게시글 상태입니다: " + value);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
