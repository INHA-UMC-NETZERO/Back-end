package com.inhabada.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageProfileSummary(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "사용자 닉네임", example = "홍길동")
        String nickname,
        @Schema(description = "사용자 이메일", example = "student@inha.edu")
        String email,
        @Schema(description = "사용자 소속. 현재 User 엔티티에 소속 컬럼이 없으면 null입니다.", nullable = true, example = "공과대학")
        String affiliation,
        @Schema(description = "프로필 이미지 URL. 현재 User 엔티티에 프로필 이미지 컬럼이 없으면 null입니다.", nullable = true)
        String profileImageUrl
) {
}
