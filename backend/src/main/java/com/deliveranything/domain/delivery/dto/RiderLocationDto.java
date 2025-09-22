package com.deliveranything.domain.delivery.dto;

import lombok.Builder;

@Builder
public record RiderLocationDto(
    String riderProfileId,  // 추후 JWT에서 사용자 정보-Profile ID가 넘어오면 변경 예정
    double latitude,
    double longitude,
    Long timestamp
) {

}
