package com.deliveranything.domain.order.dto;

import com.deliveranything.domain.order.entity.Order;

public record OrderCreateResponse(OrderResponse order, Long paymentId) {

  public static OrderCreateResponse of(Order order, Long paymentId) {
    return new OrderCreateResponse(OrderResponse.from(order), paymentId);
  }
}
