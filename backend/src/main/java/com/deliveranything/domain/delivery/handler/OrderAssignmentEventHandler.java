package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.delivery.event.event.redis.OrderAssignmentRedisPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderAssignmentEventHandler {

  private final OrderAssignmentRedisPublisher orderAssignmentRedisPublisher; // Kafka → Redis 변경

  // 주문 도메인에서 발행한 이벤트 수신
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderAssignment(List<RiderNotificationDto> orderEvent) {
    orderAssignmentRedisPublisher.publish(orderEvent);
  }
}