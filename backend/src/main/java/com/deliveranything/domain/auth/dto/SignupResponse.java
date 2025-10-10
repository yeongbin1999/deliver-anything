package com.deliveranything.domain.auth.dto;

import lombok.Builder;

@Builder
public record SignupResponse(
    Long userId,
    String email,
    String username,
    boolean isOnboardingCompleted
) {

}