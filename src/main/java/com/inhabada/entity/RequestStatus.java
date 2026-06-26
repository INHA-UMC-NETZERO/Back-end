package com.inhabada.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestStatus {
    APPLIED("신청중"),
    PENDING("예약중"),
    REJECTED("거절됨"),
    COMPLETED("완료");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    @JsonCreator
    public static RequestStatus from(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        for (RequestStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized) || status.label.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 신청 상태입니다: " + value);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
