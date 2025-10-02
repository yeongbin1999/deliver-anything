package com.deliveranything.domain.auth.dto;

import com.deliveranything.domain.user.profile.enums.ProfileType;
import java.util.List;
import lombok.Builder;

@Builder
public record LoginResponse(
    Long userId,
    String email,
    String name,
    ProfileType currentActiveProfileType,
    Long currentActiveProfileId,
    boolean isOnboardingCompleted,
    List<ProfileType> availableProfiles
) {

}