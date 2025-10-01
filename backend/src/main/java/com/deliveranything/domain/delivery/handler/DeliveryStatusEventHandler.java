package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.delivery.event.event.redis.DeliveryStatusRedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DeliveryStatusEventHandler {

  private final DeliveryStatusRedisPublisher deliveryStatusRedisPublisher; // Kafka → Redis 변경

  // 주문 도메인에서 발행한 이벤트 수신
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDeliveryStatus(DeliveryStatusEvent orderEvent) {
    deliveryStatusRedisPublisher.publish(orderEvent);
  }
}