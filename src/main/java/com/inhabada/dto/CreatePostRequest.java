package com.inhabada.dto;

import com.inhabada.entity.Category;
import com.inhabada.entity.SubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "제목은 필수 항목입니다")
        @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다")
        @Schema(description = "게시글 제목", example = "남은 과자 나눔")
        String title,

        @NotBlank(message = "설명은 필수 항목입니다")
        @Schema(description = "게시글 상세 설명", example = "행사 후 남은 과자를 나눔합니다.")
        String description,

        @NotNull(message = "카테고리는 필수 항목입니다")
        @Schema(description = "카테고리 한글 label. 기존 영어 enum code도 호환됩니다.", example = "식품")
        Category category,

        @Schema(description = "서브카테고리 한글 label. 기타 카테고리는 비워둘 수 있습니다.", example = "과자")
        SubCategory subCategory,

        @NotNull(message = "사진은 필수 항목입니다")
        @Size(min = 1, max = 5, message = "사진은 1개 이상 5개 이하여야 합니다")
        @Schema(description = "업로드 API에서 받은 이미지 key 목록", example = "[\"posts/snack.jpg\"]")
        List<String> imageKeys,

        @NotNull(message = "총 수량은 필수 항목입니다")
        @Min(value = 1, message = "총 수량은 1 이상 99 이하여야 합니다")
        @Max(value = 99, message = "총 수량은 1 이상 99 이하여야 합니다")
        @Schema(description = "전체 나눔 수량", example = "10")
        Integer totalQuantity,

        @NotBlank(message = "보관 위치는 필수 항목입니다")
        @Size(max = 100, message = "보관 위치는 100자 이하여야 합니다")
        @Schema(description = "나눔 장소 또는 보관 위치", example = "5호관 로비")
        String location,

        @NotBlank(message = "나눔 가능 시간은 필수 항목입니다")
        @Size(max = 500, message = "나눔 가능 시간은 500자 이하여야 합니다")
        @Schema(description = "나눔 가능 시간", example = "평일 오후 3시 이후")
        String availableTime
) {
}
