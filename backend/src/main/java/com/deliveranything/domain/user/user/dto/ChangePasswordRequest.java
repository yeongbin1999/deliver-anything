package com.deliveranything.domain.user.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "현재 비밀번호는 필수 입력 사항입니다.")
    String currentPassword,

    @NotBlank(message = "새 비밀번호는 필수 입력 사항입니다.")
    @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    String newPassword
) {

}