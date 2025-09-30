package com.deliveranything.domain.delivery.dto.response;

import com.deliveranything.domain.user.enums.RiderToggleStatus;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TodayDeliveringDto(
    LocalDateTime now,
    RiderToggleStatus currentStatus,
    Integer todayDeliveryCount,
    Integer todayEarningAmount,
    Double avgDeliveryTime
) {

}
