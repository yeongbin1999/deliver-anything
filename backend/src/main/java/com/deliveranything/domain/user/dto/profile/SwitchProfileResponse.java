package com.deliveranything.domain.user.dto.profile;

import com.deliveranything.domain.user.enums.ProfileType;
import lombok.Builder;

/**
 * 프로필 전환 응답 DTO
 */
// 내부 전용 DTO
@Builder
public record SwitchProfileResponse(
    Long userId,
    ProfileType previousProfileType,
    Long previousProfileId,
    ProfileType currentProfileType,
    Long currentProfileId,
    String accessToken,      // 내부 전용
    String refreshToken      // 내부 전용
) {

  // API 응답용 변환 - 토큰 제거
  public SwitchProfileResponse toResponse() {
    return SwitchProfileResponse.builder()
        .userId(userId)
        .previousProfileType(previousProfileType)
        .previousProfileId(previousProfileId)
        .currentProfileType(currentProfileType)
        .currentProfileId(currentProfileId)
        .build();
  }
}