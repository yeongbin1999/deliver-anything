package com.deliveranything.domain.delivery.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record DeliveredDetailsDto(
    LocalDateTime completedAt,
    String storeName,
    Long orderId,
    String customerAddress,
    String settlementStatus,
    Integer deliveryCharge
) {

}
