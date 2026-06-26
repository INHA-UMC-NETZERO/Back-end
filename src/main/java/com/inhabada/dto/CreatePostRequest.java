package com.inhabada.dto;

import com.inhabada.entity.Category;
import com.inhabada.entity.SubCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "제목은 필수 항목입니다")
        @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다")
        String title,

        @NotBlank(message = "설명은 필수 항목입니다")
        String description,

        @NotNull(message = "카테고리는 필수 항목입니다")
        Category category,

        SubCategory subCategory,

        @NotNull(message = "사진은 필수 항목입니다")
        @Size(min = 1, max = 5, message = "사진은 1개 이상 5개 이하여야 합니다")
        List<String> imageKeys,

        @NotNull(message = "총 수량은 필수 항목입니다")
        @Min(value = 1, message = "총 수량은 1 이상 99 이하여야 합니다")
        @Max(value = 99, message = "총 수량은 1 이상 99 이하여야 합니다")
        Integer totalQuantity,

        @NotBlank(message = "보관 위치는 필수 항목입니다")
        @Size(max = 100, message = "보관 위치는 100자 이하여야 합니다")
        String location,

        @NotBlank(message = "나눔 가능 시간은 필수 항목입니다")
        @Size(max = 500, message = "나눔 가능 시간은 500자 이하여야 합니다")
        String availableTime
) {
}
