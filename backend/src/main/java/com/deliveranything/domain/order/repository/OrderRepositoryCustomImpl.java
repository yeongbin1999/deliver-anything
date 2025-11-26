package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.QOrder;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QOrder order = QOrder.order;

  @Override
  public List<Order> findOrdersByStoreIdWithCursor(
      Long storeId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  ) {
    return fetchOrders(
        size,
        cursorCondition(lastCreatedAt, lastOrderId),
        order.storeId.eq(storeId),
        statusIn(statuses)
    );
  }

  @Override
  public List<Order> findOrdersByCustomerProfileIdWithCursor(
      Long customerProfileId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  ) {
    return fetchOrders(
        size,
        cursorCondition(lastCreatedAt, lastOrderId),
        order.customerProfileId.eq(customerProfileId),
        statusIn(statuses)
    );
  }

  private List<Order> fetchOrders(long size, BooleanExpression... conditions) {
    return queryFactory.selectFrom(order)
        .where(conditions)
        .orderBy(order.createdAt.desc(), order.id.desc())
        .limit(size)
        .fetch();
  }

  private BooleanExpression statusIn(List<OrderStatus> statuses) {
    return statuses != null && !statuses.isEmpty() ? order.status.in(statuses) : null;
  }

  private BooleanExpression cursorCondition(LocalDateTime lastCreatedAt, Long lastOrderId) {
    if (lastCreatedAt == null || lastOrderId == null) {
      return null;
    }

    return order.createdAt.lt(lastCreatedAt)
        .or(order.createdAt.eq(lastCreatedAt).and(order.id.lt(lastOrderId)));
  }
}
