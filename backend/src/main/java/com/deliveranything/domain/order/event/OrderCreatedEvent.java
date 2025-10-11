package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.event.dto.OrderItemInfo;
import java.util.List;

public record OrderCreatedEvent(
    Long orderId,
    String merchantUid,
    Long totalPrice,
    List<OrderItemInfo> orderItems
) {

  public static OrderCreatedEvent from(Order order) {
    return new OrderCreatedEvent(
        order.getId(),
        order.getMerchantId(),
        order.getTotalPrice(),
        order.getOrderItems().stream().map(OrderItemInfo::fromOrderItem).toList()
    );
  }
}