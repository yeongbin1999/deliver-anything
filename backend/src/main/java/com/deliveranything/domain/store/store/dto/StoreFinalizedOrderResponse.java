package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record StoreFinalizedOrderResponse(
    Long orderId,
    List<OrderItem> orderItems,
    OrderStatus status,
    String address,
    String storeNote,
    LocalDateTime createdAt
) {

  public static StoreFinalizedOrderResponse from(Order order) {
    return new StoreFinalizedOrderResponse(
        order.getId(),
        order.getOrderItems(),
        order.getStatus(),
        order.getAddress(),
        order.getStoreNote(),
        order.getCreatedAt()
    );
  }
}
