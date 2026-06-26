package com.inhabada.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubCategory {
    CUP_RAMEN(Category.FOOD, "컵라면", 400),
    SNACK(Category.FOOD, "과자", 250),
    CANDY(Category.FOOD, "사탕", 150),
    CHOCOLATE(Category.FOOD, "초콜릿", 600),
    JELLY(Category.FOOD, "젤리", 150),

    WATER(Category.DRINK, "생수", 110),
    SODA(Category.DRINK, "탄산음료", 240),
    ION_DRINK(Category.DRINK, "이온음료", 240),
    COFFEE(Category.DRINK, "커피", 200),

    NAME_BADGE(Category.STATIONERY_EVENT, "명찰", 80),
    NAME_TAG(Category.STATIONERY_EVENT, "네임택", 30),
    PROMOTIONAL_ITEM(Category.STATIONERY_EVENT, "홍보용품", 150),

    BOX(Category.PACKING_ORGANIZING, "박스", 300),
    TAPE(Category.PACKING_ORGANIZING, "테이프", 250),
    STORAGE_BOX(Category.PACKING_ORGANIZING, "보관함", 1_500),
    CLIP(Category.PACKING_ORGANIZING, "집게", 20),

    CHAIR(Category.FURNITURE_SPACE, "의자", 20_000),
    TABLE(Category.FURNITURE_SPACE, "테이블", 25_000),
    BOARD(Category.FURNITURE_SPACE, "게시판", 8_000),
    SHELF(Category.FURNITURE_SPACE, "선반", 15_000);

    private final Category category;
    private final String label;
    private final int unitCarbonSavingGram;

    SubCategory(Category category, String label, int unitCarbonSavingGram) {
        this.category = category;
        this.label = label;
        this.unitCarbonSavingGram = unitCarbonSavingGram;
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

    public int getUnitCarbonSavingGram() {
        return unitCarbonSavingGram;
    }

    public boolean belongsTo(Category category) {
        return this.category == category;
    }
}
