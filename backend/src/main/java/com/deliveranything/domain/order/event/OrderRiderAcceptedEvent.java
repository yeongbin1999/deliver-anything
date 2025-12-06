package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;

public record OrderRiderAcceptedEvent(
    Long orderId,
    Long riderId,
    Long customerProfileId,
    Long sellerProfileId,
    OrderStatus status,
    Double eta
) {

  public static OrderRiderAcceptedEvent fromOrderAndETA(Order order, Long riderId, Double eta) {
    return new OrderRiderAcceptedEvent(
        order.getId(),
        riderId,
        order.getCustomerProfileId(),
        order.getStore().getSellerProfileId(),
        order.getStatus(),
        eta
    );
  }
}
