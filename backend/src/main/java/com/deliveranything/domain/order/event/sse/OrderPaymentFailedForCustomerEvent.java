package com.deliveranything.domain.order.event.sse;

import com.deliveranything.domain.order.entity.Order;

public record OrderPaymentFailedForCustomerEvent(Long customerId) {

  public static OrderPaymentFailedForCustomerEvent fromOrder(Order order) {
    return new OrderPaymentFailedForCustomerEvent(order.getCustomer().getId());
  }
}
