package com.deliveranything.domain.user.profile.dto.onboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 판매자 프로필 온보딩 데이터
 */
public record SellerOnboardingData(
    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    String nickname,

    @NotBlank(message = "사업자명은 필수 입력 사항입니다.")
    @Size(max = 100, message = "사업자명은 100자 이하로 입력해주세요.")
    String businessName,

    @NotBlank(message = "사업자등록번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    String businessCertificateNumber,

    @NotBlank(message = "사업자 전화번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    String businessPhoneNumber,

    @NotBlank(message = "은행명은 필수 입력 사항입니다.")
    @Size(max = 50, message = "은행명은 50자 이하로 입력해주세요.")
    String bankName,

    @NotBlank(message = "계좌번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^\\d{10,14}$", message = "계좌번호는 10~14자리 숫자여야 합니다.")
    String accountNumber,

    @NotBlank(message = "예금주는 필수 입력 사항입니다.")
    @Size(max = 50, message = "예금주는 50자 이하로 입력해주세요.")
    String accountHolder,

    String profileImageUrl  // 선택사항
) {

}