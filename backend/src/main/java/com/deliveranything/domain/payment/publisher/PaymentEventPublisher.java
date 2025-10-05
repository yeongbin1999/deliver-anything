package com.deliveranything.domain.payment.publisher;

import com.deliveranything.domain.payment.event.PaymentCompletedEvent;
import com.deliveranything.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
    redisTemplate.convertAndSend("payment-completed-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentFailedEvent(PaymentFailedEvent event) {
    redisTemplate.convertAndSend("payment-failed-event", event);
  }
}
