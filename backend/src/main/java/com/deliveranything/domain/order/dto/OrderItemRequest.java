package com.deliveranything.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
    @NotNull @Positive Long productId,
    @NotNull @Positive Long price,
    @NotNull @Positive Long quantity
) {

}
