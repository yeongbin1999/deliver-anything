package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.event.dto.OrderItemInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record OrderCreatedEvent(
    Long orderId,
    String merchantUid,
    BigDecimal totalPrice,
    List<OrderItemInfo> orderItems
) {

  public static OrderCreatedEvent from(Order order) {
    return new OrderCreatedEvent(
        order.getId(),
        order.getMerchantId(),
        order.getTotalPrice(),
        order.getOrderItems().stream()
            .map(orderItem -> new OrderItemInfo(
                orderItem.getProduct().getId(),
                orderItem.getQuantity()
            ))
            .collect(Collectors.toList())
    );
  }
}