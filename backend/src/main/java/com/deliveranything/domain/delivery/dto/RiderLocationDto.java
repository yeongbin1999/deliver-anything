package com.deliveranything.domain.delivery.dto;

import lombok.Builder;

@Builder
public record RiderLocationDto(
    Long riderProfileId,
    // Redis GEO에서는 String을 키로 사용하므로 서비스에서 변환
    // JWT 에서 사용자 정보-Profile ID가 넘어오면 Long -> String 변환 로직 제거 예정
    double latitude,
    double longitude,
    long timestamp  // 밀리초 단위,
    // 변환: Instant.ofEpochMilli(timestamp) → LocalDateTime
) {

}
