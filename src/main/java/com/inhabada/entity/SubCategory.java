package com.inhabada.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubCategory {
    CUP_RAMEN(Category.FOOD, "컵라면"),
    SNACK(Category.FOOD, "과자"),
    CANDY(Category.FOOD, "사탕"),
    CHOCOLATE(Category.FOOD, "초콜릿"),
    JELLY(Category.FOOD, "젤리"),

    WATER(Category.DRINK, "생수"),
    SODA(Category.DRINK, "탄산음료"),
    ION_DRINK(Category.DRINK, "이온음료"),
    COFFEE(Category.DRINK, "커피"),

    NAME_BADGE(Category.STATIONERY_EVENT, "명찰"),
    NAME_TAG(Category.STATIONERY_EVENT, "네임택"),
    PROMOTIONAL_ITEM(Category.STATIONERY_EVENT, "홍보용품"),

    BOX(Category.PACKING_ORGANIZING, "박스"),
    TAPE(Category.PACKING_ORGANIZING, "테이프"),
    STORAGE_BOX(Category.PACKING_ORGANIZING, "보관함"),
    CLIP(Category.PACKING_ORGANIZING, "집게"),

    CHAIR(Category.FURNITURE_SPACE, "의자"),
    TABLE(Category.FURNITURE_SPACE, "테이블"),
    BOARD(Category.FURNITURE_SPACE, "게시판"),
    SHELF(Category.FURNITURE_SPACE, "선반");

    private final Category category;
    private final String label;

    SubCategory(Category category, String label) {
        this.category = category;
        this.label = label;
    }

    @JsonCreator
    public static SubCategory from(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        for (SubCategory subCategory : values()) {
            if (subCategory.name().equalsIgnoreCase(normalized) || subCategory.label.equals(normalized)) {
                return subCategory;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 하위 카테고리입니다: " + value);
    }

    public Category getCategory() {
        return category;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public boolean belongsTo(Category category) {
        return this.category == category;
    }
}
