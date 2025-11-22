package com.deliveranything.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record OrderRiderAcceptRequest(
    @NotNull
    @Schema(description = "라이더 예상 도착 시간 (분)")
    Double etaMinutes
) {

}
