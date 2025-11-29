package com.deliveranything.domain.payment.subscirber;

import com.deliveranything.domain.payment.handler.OrderEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
import com.deliveranything.global.event.AbstractRedisEventSubscriber;
import com.deliveranything.global.event.RedisEventHandler;
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
