package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

  List<Order> findOrdersByStoreIdWithCursor(
      Long storeId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  );

  List<Order> findOrdersByCustomerProfileIdWithCursor(
      Long customerProfileId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  );
}
