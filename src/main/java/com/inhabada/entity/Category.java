package com.inhabada.entity;

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

    public String getLabel() {
        return label;
    }
}
