package com.deliveranything.domain.delivery.dto.response;

import lombok.Builder;

@Builder
public record CurrentDeliveringResponseDto(
    Long orderId,
    String storeName,
    String customerAddress,
    Double remainingTime
) {

}
