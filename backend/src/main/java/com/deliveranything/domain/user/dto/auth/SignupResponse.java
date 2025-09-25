package com.deliveranything.domain.user.dto.auth;

import lombok.Builder;

@Builder
public record SignupResponse(
    Long userId,
    String email,
    String name,
    boolean isOnboardingCompleted
) {

}