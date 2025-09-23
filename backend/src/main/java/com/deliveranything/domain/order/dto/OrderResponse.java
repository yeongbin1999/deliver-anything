package com.deliveranything.domain.order.dto;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    List<OrderItem> orderItems,
    OrderStatus orderStatus,
    String merchantId,
    String address,
    String riderNote,
    String storeNote,
    BigDecimal totalPrice,
    BigDecimal storePrice,
    BigDecimal deliveryPrice,
    LocalDateTime orderDate
) {

  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getOrderItems(),
        order.getOrderStatus(),
        order.getMerchantId(),
        order.getAddress(),
        order.getRiderNote(),
        order.getStoreNote(),
        order.getTotalPrice(),
        order.getStorePrice(),
        order.getDeliveryPrice(),
        order.getCreatedAt()
    );
  }
}
