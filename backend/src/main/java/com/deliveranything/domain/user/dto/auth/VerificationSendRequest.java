package com.deliveranything.domain.user.dto.auth;

import com.deliveranything.domain.user.enums.VerificationPurpose;
import com.deliveranything.domain.user.enums.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerificationSendRequest(
    @NotBlank(message = "식별자는 필수 입력 사항입니다.")
    String identifier,

    @NotNull(message = "인증 타입은 필수 입력 사항입니다.")
    VerificationType verificationType,

    @NotNull(message = "인증 목적은 필수 입력 사항입니다.")
    VerificationPurpose purpose
) {

}