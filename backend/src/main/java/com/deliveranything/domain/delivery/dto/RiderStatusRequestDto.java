package com.deliveranything.domain.delivery.dto;

import jakarta.validation.constraints.Pattern;

public record RiderStatusRequestDto(
    @Pattern(regexp = "^(ON|OFF)$", message = "라이더 상태는 반드시 'ON' 또는 'OFF' 여야 합니다.")
    String riderStatus
) {

}
