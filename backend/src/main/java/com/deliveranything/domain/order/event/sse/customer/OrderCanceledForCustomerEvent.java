package com.deliveranything.domain.order.event.sse.customer;

import com.deliveranything.domain.order.entity.Order;

public record OrderCanceledForCustomerEvent(Long customerId) {

  public static OrderCanceledForCustomerEvent fromOrder(Order order) {
    return new OrderCanceledForCustomerEvent(order.getCustomer().getId());
  }
}
