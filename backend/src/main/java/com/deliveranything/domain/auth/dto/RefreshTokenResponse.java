package com.deliveranything.domain.auth.dto;

import lombok.Builder;

@Builder
public record RefreshTokenResponse(
    String accessToken, // 쿠키 + 헤더 방식을 사용하기 위해서 응답 바디에 포함했습니다!
    Long expiresIn  // 선택사항: 만료 시간(초)
) {

}