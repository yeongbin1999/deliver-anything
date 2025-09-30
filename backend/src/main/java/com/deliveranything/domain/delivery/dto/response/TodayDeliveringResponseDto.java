package com.deliveranything.domain.delivery.dto.response;

import com.deliveranything.domain.user.enums.RiderToggleStatus;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TodayDeliveringResponseDto(
    LocalDateTime now,
    RiderToggleStatus currentStatus,
    Long todayDeliveryCount,
    Integer todayEarningAmount,
    Double avgDeliveryTime
) {

}
