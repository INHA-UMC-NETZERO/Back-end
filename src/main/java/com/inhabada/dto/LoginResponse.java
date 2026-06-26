package com.inhabada.dto;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String nickname
) {
}
