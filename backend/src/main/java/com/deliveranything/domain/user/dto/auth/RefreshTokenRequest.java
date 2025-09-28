package com.deliveranything.domain.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "리프레시 토큰은 필수 입력 사항입니다.")
    String refreshToken
) {

}