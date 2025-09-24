package com.deliveranything.domain.delivery.dto;

import com.deliveranything.domain.order.dto.OrderDetailsDto;
import com.deliveranything.domain.order.enums.OrderStatus;
import lombok.Builder;

@Builder
public record RiderNotificationDto(
    OrderDetailsDto orderDetailsDto,
    String riderId,
    double etaMinutes, // riderId → ETA (분)
    OrderStatus orderDeliveryStatus
) {

}
