package com.inhabada.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    FOOD("식품"),
    DRINK("음료"),
    STATIONERY_EVENT("문구/행사"),
    PACKING_ORGANIZING("포장/정리"),
    FURNITURE_SPACE("가구/공간"),
    ETC("기타");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    @JsonCreator
    public static Category from(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        for (Category category : values()) {
            if (category.name().equalsIgnoreCase(normalized) || category.label.equals(normalized)) {
                return category;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + value);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
