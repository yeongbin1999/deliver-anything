package com.deliveranything.domain.order.event.sse.customer;

import com.deliveranything.domain.order.entity.Order;

public record OrderCreateFailedForCustomerEvent(Long customerProfileId, Long orderId) {

  public static OrderCreateFailedForCustomerEvent fromOrder(Order order) {
    return new OrderCreateFailedForCustomerEvent(order.getCustomerProfileId(), order.getId());
  }
}
