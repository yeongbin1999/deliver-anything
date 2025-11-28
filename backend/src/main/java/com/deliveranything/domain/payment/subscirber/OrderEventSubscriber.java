package com.deliveranything.domain.payment.subscirber;

import com.deliveranything.domain.order.handler.RedisEventHandler;
import com.deliveranything.domain.order.subscriber.AbstractRedisEventSubscriber;
import com.deliveranything.domain.payment.handler.OrderEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventSubscriber extends AbstractRedisEventSubscriber {

  private final OrderEventHandler orderEventHandler;

  @Override
  protected RedisTopicPattern getTopicPattern() {
    return RedisTopicPattern.ORDER_EVENTS;
  }

  @Override
  protected RedisEventHandler getHandler() {
    return orderEventHandler;
  }
}
