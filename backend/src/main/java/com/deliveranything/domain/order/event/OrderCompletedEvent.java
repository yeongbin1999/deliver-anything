package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import java.math.BigDecimal;

public record OrderCompletedEvent(
    Long orderId,
    Long riderProfileId,
    Long sellerProfileId,
    BigDecimal storePrice,
    BigDecimal deliveryPrice
) {

  public static OrderCompletedEvent fromOrder(Order order, Long riderId, Long sellerId) {
    return new OrderCompletedEvent(
        order.getId(),
        riderId,
        sellerId,
        order.getStorePrice(),
        order.getDeliveryPrice()
    );
  }
}
