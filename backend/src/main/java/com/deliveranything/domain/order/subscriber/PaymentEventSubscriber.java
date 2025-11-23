package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.PaymentEventHandler;
import com.deliveranything.domain.order.handler.RedisEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
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
