package com.deliveranything.domain.delivery.publisher;

import com.deliveranything.domain.delivery.event.dto.DeliveryOfferedToRidersEvent;
import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  // 배달 상태 변경 이벤트 발행
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDeliveryStatusEvent(DeliveryStatusEvent event) {
    redisTemplate.convertAndSend("delivery-status-event", event);
  }

  // 상점에서 수락한 주문 주위 라이더들에게 전부 제안 시 발행
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDeliveryOfferedToRidersEvent(DeliveryOfferedToRidersEvent event) {
    redisTemplate.convertAndSend("delivery-offered-to-riders-event", event);
  }
}
