package com.deliveranything.domain.order.event;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.Publisher;
import com.deliveranything.domain.order.event.dto.OrderItemInfo;
import java.util.List;

public record OrderRejectedEvent(
    Long orderId,
    String merchantUid,
    String cancelReason,
    List<OrderItemInfo> orderItems,
    Publisher publisher
) {

  public static OrderRejectedEvent from(Order order, String cancelReason, Publisher publisher) {
    return new OrderRejectedEvent(
        order.getId(),
        order.getMerchantId(),
        cancelReason,
        order.getOrderItems().stream()
            .map(OrderItemInfo::fromOrderItem)
            .toList(),
        publisher
    );
  }
}
