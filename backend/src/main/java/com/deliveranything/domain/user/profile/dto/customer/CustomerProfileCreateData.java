package com.deliveranything.domain.user.profile.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 소비자 프로필 생성 데이터 UI: 닉네임, 프로필사진, 전화번호. *배송지는 프로필 생성 후 별도 API로 추가
 */
public record CustomerProfileCreateData(
    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    String nickname,

    String profileImageUrl,  // 선택사항

    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    String customerPhoneNumber  // 선택사항
) {

}