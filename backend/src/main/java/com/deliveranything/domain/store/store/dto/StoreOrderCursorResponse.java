package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record StoreOrderCursorResponse(
    Long orderId,
    List<OrderItem> orderItems,
    OrderStatus status,
    String address,
    String storeNote,
    LocalDateTime createdAt
) {

  public static StoreOrderCursorResponse from(OrderResponse orderResponse) {
    return new StoreOrderCursorResponse(
        orderResponse.id(),
        orderResponse.orderItems(),
        orderResponse.status(),
        orderResponse.address(),
        orderResponse.storeNote(),
        orderResponse.createdAt()
    );
  }
}
