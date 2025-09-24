package com.deliveranything.domain.order.dto;

import lombok.Builder;

@Builder
public record OrderDetailsDto(
    String orderId,
    String storeName,
    Double distance,
    Integer expectedCharge
) {

}
