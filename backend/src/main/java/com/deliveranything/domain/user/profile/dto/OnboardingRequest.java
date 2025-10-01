package com.deliveranything.domain.user.profile.dto;

import com.deliveranything.domain.user.profile.enums.ProfileType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record OnboardingRequest(
    @NotNull(message = "프로필 타입은 필수입니다.")
    ProfileType selectedProfile,

    @NotNull(message = "프로필 데이터는 필수입니다.")
    Map<String, Object> profileData
) {

}