package com.deliveranything.domain.user.profile.dto.onboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 고객 프로필 온보딩 데이터
 */
public record CustomerOnboardingData(
    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    String nickname,

    String profileImageUrl  // 선택사항
) {

}