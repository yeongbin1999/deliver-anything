package com.deliveranything.domain.user.dto.auth;

import com.deliveranything.domain.user.enums.ProfileType;
import java.util.List;
import lombok.Builder;

@Builder
public record LoginResponse(
    Long userId,
    String email,
    String name,
    String accessToken,
    String refreshToken,
    ProfileType currentActiveProfileType,
    Long currentActiveProfileId,
    boolean isOnboardingCompleted,
    List<ProfileType> availableProfiles
) {

}