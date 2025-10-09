package com.deliveranything.domain.order.event.sse;

import com.deliveranything.domain.order.entity.Order;

public record OrderCanceledForCustomerEvent(Long customerId) {

  public static OrderCanceledForCustomerEvent fromOrder(Order order) {
    return new OrderCanceledForCustomerEvent(order.getCustomer().getId());
  }
}
