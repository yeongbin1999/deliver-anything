package com.deliveranything.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemRequest(
    @NotNull @Positive Long productId,
    @NotNull @Positive BigDecimal price,
    @Positive int quantity
) {

}
