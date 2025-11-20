package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.event.dto.OrderItemInfo;
import java.util.List;

public record OrderStoreAcceptedEvent(
    String orderId,
    List<OrderItemInfo> orderItems,
    Long sellerId,
    Long storeId,
    String storeName,
    Double storeLon,
    Double storeLat,
    Double customerLon,
    Double customerLat
) {

  public static OrderStoreAcceptedEvent from(Order order) {
    return new OrderStoreAcceptedEvent(
        order.getId().toString(),
        order.getOrderItems().stream().map(OrderItemInfo::fromOrderItem).toList(),
        order.getStore().getSellerProfileId(),
        order.getStore().getId(),
        order.getStore().getName(),
        order.getStore().getLocation().getX(),
        order.getStore().getLocation().getY(),
        order.getDestination().getX(),
        order.getDestination().getY()
    );
  }
}
