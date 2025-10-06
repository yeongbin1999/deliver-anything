package com.deliveranything.domain.order.event.dto;

public record OrderItemInfo(
    Long productId,
    int quantity
) {

}
