package com.deliveranything.domain.user.profile.dto;

import com.deliveranything.domain.user.profile.enums.ProfileType;
import lombok.Builder;

/**
 * 추가 프로필 생성 응답 DTO
 */
@Builder
public record AddProfileResponse(
    Long userId,
    ProfileType profileType,
    Long profileId,
    String nickname,
    boolean isActive,
    String message
) {

}