package com.deliveranything.domain.order.event.sse.customer;

import com.deliveranything.domain.order.entity.Order;

public record OrderCanceledForCustomerEvent(Long customerProfileId, Long orderId) {

  public static OrderCanceledForCustomerEvent fromOrder(Order order) {
    return new OrderCanceledForCustomerEvent(order.getCustomerProfileId(), order.getId());
  }
}
