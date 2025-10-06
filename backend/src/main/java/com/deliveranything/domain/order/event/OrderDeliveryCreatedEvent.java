package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.event.dto.OrderItemInfo;
import java.util.List;
import java.util.stream.Collectors;

public record OrderDeliveryCreatedEvent(
    Long orderId,
    List<OrderItemInfo> orderItems,
    String storeName,
    double storeLon,
    double storeLat,
    double customerLon,
    double customerLat
) {

  public static OrderDeliveryCreatedEvent from(Order order) {
    return new OrderDeliveryCreatedEvent(
        order.getId(),
        order.getOrderItems().stream()
            .map(orderItem -> new OrderItemInfo(
                orderItem.getProduct().getId(),
                orderItem.getQuantity()
            ))
            .collect(Collectors.toList()),
        order.getStore().getName(),
        order.getStore().getLocation().getX(),
        order.getStore().getLocation().getY(),
        order.getDestination().getX(),
        order.getDestination().getY()
    );
  }
}
