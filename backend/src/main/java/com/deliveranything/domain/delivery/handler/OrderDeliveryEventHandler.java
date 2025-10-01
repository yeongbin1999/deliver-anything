package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderDeliveryCreatedEvent;
import com.deliveranything.domain.delivery.event.event.redis.OrderAssignmentRedisPublisher;
import com.deliveranything.domain.delivery.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderDeliveryEventHandler {

  private final OrderNotificationService orderNotificationService;
  private final OrderAssignmentRedisPublisher orderAssignmentRedisPublisher; // Kafka → Redis 변경

  // 주문 도메인에서 발행한 이벤트 수신
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreated(OrderDeliveryCreatedEvent orderEvent) {
    orderNotificationService.processOrderEvent(orderEvent)
        .subscribe(orderAssignmentRedisPublisher::publish); // Kafka → Redis 변경
  }
}