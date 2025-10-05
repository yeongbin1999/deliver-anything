package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.OrderItem;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderAcceptedEvent(
    Long id,
    List<OrderItem> orderItems,
    OrderStatus status,
    String merchantId,
    String storeName,
    String address,
    String riderNote,
    String storeNote,
    BigDecimal totalPrice,
    BigDecimal storePrice,
    BigDecimal deliveryPrice,
    LocalDateTime createdAt
) {

  public static OrderAcceptedEvent from(Order order) {
    return new OrderAcceptedEvent(
        order.getId(),
        order.getOrderItems(),
        order.getStatus(),
        order.getMerchantId(),
        order.getStore().getName(),
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
