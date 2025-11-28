package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.PaymentEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
import com.deliveranything.global.event.AbstractRedisEventSubscriber;
import com.deliveranything.global.event.RedisEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventSubscriber extends AbstractRedisEventSubscriber {

  private final PaymentEventHandler paymentEventHandler;

  @Override
  protected RedisTopicPattern getTopicPattern() {
    return RedisTopicPattern.PAYMENT_EVENTS;
  }

  @Override
  protected RedisEventHandler getHandler() {
    return paymentEventHandler;
  }
}
