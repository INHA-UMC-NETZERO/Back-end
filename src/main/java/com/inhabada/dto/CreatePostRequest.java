package com.inhabada.dto;

import jakarta.validation.Valid;
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

        @NotBlank(message = "카테고리는 필수 항목입니다")
        String category,

        @NotNull(message = "사진은 필수 항목입니다")
        @Size(min = 1, max = 5, message = "사진은 1장 이상 5장 이하여야 합니다")
        List<String> imageKeys,

        @NotNull(message = "총 수량은 필수 항목입니다")
        @Min(value = 1, message = "총 수량은 1 이상 99 이하여야 합니다")
        @Max(value = 99, message = "총 수량은 1 이상 99 이하여야 합니다")
        Integer totalQuantity,

        @NotNull(message = "수령 가능 일정은 필수 항목입니다")
        @Size(min = 1, max = 10, message = "수령 가능 일정은 1개 이상 10개 이하여야 합니다")
        @Valid
        List<SlotRequest> slots
) {
}
