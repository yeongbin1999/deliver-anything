package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.QOrder;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.store.store.entity.QStore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public List<Order> findOrdersWithStoreByCustomerId(
      Long customerId,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  ) {
    return fetchOrders(
        size,
        cursorCondition(lastCreatedAt, lastOrderId),
        QOrder.order.customer.id.eq(customerId)
    );
  }

  public List<Order> findOrdersWithStoreByStoreId(
      Long storeId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  ) {
    return fetchOrders(
        size,
        cursorCondition(lastCreatedAt, lastOrderId),
        QOrder.order.store.id.eq(storeId),
        statusIn(statuses)
    );
  }

  public List<Order> findOrdersWithStoreByCustomerId(
      Long customerId,
      List<OrderStatus> statuses,
      LocalDateTime lastCreatedAt,
      Long lastOrderId,
      long size
  ) {
    return fetchOrders(
        size,
        cursorCondition(lastCreatedAt, lastOrderId),
        QOrder.order.customer.id.eq(customerId),
        statusIn(statuses)
    );
  }

  private List<Order> fetchOrders(long size, BooleanExpression... conditions) {
    QOrder order = QOrder.order;
    QStore store = QStore.store;

    return queryFactory.selectFrom(order)
        .join(order.store, store).fetchJoin()
        .where(conditions)
        .orderBy(order.createdAt.desc(), order.id.desc())
        .limit(size)
        .fetch();
  }

  private BooleanExpression statusIn(List<OrderStatus> statuses) {
    return statuses != null && !statuses.isEmpty() ? QOrder.order.status.in(statuses) : null;
  }

  // 최신순 커서
  private BooleanExpression cursorCondition(LocalDateTime lastCreatedAt, Long lastOrderId) {
    // 첫 페이지 조회 시 커서 조건 없음
    if (lastCreatedAt == null || lastOrderId == null) {
      return null;
    }

    QOrder order = QOrder.order;

    // 1. 이전에 생성한 레코드 추출
    // 2. 생성 시각이 같다면 주문 ID 작은 것들 추출
    return order.createdAt.lt(lastCreatedAt)
        .or(order.createdAt.eq(lastCreatedAt).and(order.id.lt(lastOrderId)));
  }
}
