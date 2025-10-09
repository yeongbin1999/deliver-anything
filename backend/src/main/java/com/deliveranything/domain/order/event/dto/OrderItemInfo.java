package com.deliveranything.domain.order.event.dto;

import com.deliveranything.domain.order.entity.OrderItem;

public record OrderItemInfo(
    Long productId,
    int quantity,
    int price
) {

  public static OrderItemInfo fromOrderItem(OrderItem orderItem) {
    return new OrderItemInfo(
        orderItem.getId(),
        orderItem.getQuantity(),
        orderItem.getPrice().intValue()
    );
  }
}
