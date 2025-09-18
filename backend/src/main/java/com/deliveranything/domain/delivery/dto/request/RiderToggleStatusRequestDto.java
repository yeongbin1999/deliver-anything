package com.deliveranything.domain.delivery.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RiderToggleStatusRequestDto(
    // 추후 JWT에서 사용자 정보-Profile ID가 넘어오면 변경 예정
    @NotNull
    Long riderProfileId,

    @Pattern(regexp = "^(ON|OFF)$", message = "라이더 상태는 반드시 'ON' 또는 'OFF' 여야 합니다.")
    String riderStatus
) {

}
