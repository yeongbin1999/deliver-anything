package com.deliveranything.domain.delivery.dto.response;

import lombok.Builder;

@Builder
public record CurrentDeliveringDto(
    Long orderId,
    String storeName,
    String customerAddress,
    double remainingTime
) {

}
