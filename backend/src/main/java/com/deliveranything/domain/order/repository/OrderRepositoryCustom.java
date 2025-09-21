package com.deliveranything.domain.order.repository;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.entity.QOrder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public List<Order> findCustomerOrders(Long customerId, Long cursor, int size) {
    QOrder order = QOrder.order;

    return queryFactory.selectFrom(order)
        .where(
            order.customer.id.eq(customerId),
            cursor != null ? order.id.lt(cursor) : null
        )
        .orderBy(order.id.desc())
        .limit(size)
        .fetch();
  }
}
