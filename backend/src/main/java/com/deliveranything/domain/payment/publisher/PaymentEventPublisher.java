package com.deliveranything.domain.payment.publisher;

import com.deliveranything.domain.payment.event.PaymentCancelFailedEvent;
import com.deliveranything.domain.payment.event.PaymentCancelSuccessEvent;
import com.deliveranything.domain.payment.event.PaymentFailedEvent;
import com.deliveranything.domain.payment.event.PaymentSuccessEvent;
import com.deliveranything.global.enums.RedisTopic;
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
  public void handlePaymentCompletedEvent(PaymentSuccessEvent event) {
    redisTemplate.convertAndSend(RedisTopic.PAYMENT_COMPLETED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentFailedEvent(PaymentFailedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.PAYMENT_FAILED_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCancelSuccessEvent(PaymentCancelSuccessEvent event) {
    redisTemplate.convertAndSend(RedisTopic.PAYMENT_CANCEL_SUCCESS_EVENT.getTopic(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCancelFailedEvent(PaymentCancelFailedEvent event) {
    redisTemplate.convertAndSend(RedisTopic.PAYMENT_CANCEL_FAILED_EVENT.getTopic(), event);
  }
}
