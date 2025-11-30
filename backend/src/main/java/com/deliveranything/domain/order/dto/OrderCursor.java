package com.deliveranything.domain.order.dto;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.global.util.CursorUtil;
import java.time.LocalDateTime;

public record OrderCursor(LocalDateTime createdAt, Long orderId) {

  public static OrderCursor from(Order order) {
    return new OrderCursor(order.getCreatedAt(), order.getId());
  }

  public static OrderCursor fromToken(String nextPageToken) {
    OrderCursor decodedCursor = CursorUtil.decode(nextPageToken, OrderCursor.class);
    return decodedCursor != null ? decodedCursor : new OrderCursor(null, null);
  }
}
