package com.deliveranything.domain.order.publisher;

import com.deliveranything.domain.order.event.OrderAcceptedEvent;
import com.deliveranything.domain.order.event.OrderCancelEvent;
import com.deliveranything.domain.order.event.OrderCompletedEvent;
import com.deliveranything.domain.order.event.OrderCreatedEvent;
import com.deliveranything.domain.order.event.OrderPaymentRequestedEvent;
import com.deliveranything.domain.order.event.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    redisTemplate.convertAndSend("order-created-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaymentRequestedEvent(OrderPaymentRequestedEvent event) {
    redisTemplate.convertAndSend("order-payment-requested-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderRejectedEvent(OrderRejectedEvent event) {
    redisTemplate.convertAndSend("order-rejected-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderAcceptedEvent(OrderAcceptedEvent event) {
    redisTemplate.convertAndSend("order-accepted-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCompletedEvent(OrderCompletedEvent event) {
    redisTemplate.convertAndSend("order-completed-event", event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCancelEvent(OrderCancelEvent event) {
    redisTemplate.convertAndSend("order-cancel-event", event);
  }
}
