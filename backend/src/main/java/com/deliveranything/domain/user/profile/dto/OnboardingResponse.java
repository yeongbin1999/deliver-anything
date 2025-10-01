package com.deliveranything.domain.user.profile.dto;

import com.deliveranything.domain.user.profile.enums.ProfileType;
import lombok.Builder;

@Builder
public record OnboardingResponse(
    Long userId,
    ProfileType selectedProfile,
    Long profileId,
    boolean isOnboardingCompleted
) {

}