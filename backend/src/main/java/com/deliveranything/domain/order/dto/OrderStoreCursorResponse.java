package com.deliveranything.domain.order.dto;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderStoreCursorResponse(
    Long orderId,
    List<OrderItem> orderItems,
    OrderStatus status,
    String address,
    String storeNote,
    LocalDateTime createdAt
) {

  public static OrderStoreCursorResponse from(Order order) {
    return new OrderStoreCursorResponse(
        order.getId(),
        order.getOrderItems(),
        order.getStatus(),
        order.getAddress(),
        order.getStoreNote(),
        order.getCreatedAt()
    );
  }
}
