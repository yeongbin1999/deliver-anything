package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.DeliveryEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
import com.deliveranything.global.event.AbstractRedisEventSubscriber;
import com.deliveranything.global.event.RedisEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryEventSubscriber extends AbstractRedisEventSubscriber {

  private final DeliveryEventHandler deliveryEventHandler;

  @Override
  protected RedisTopicPattern getTopicPattern() {
    return RedisTopicPattern.DELIVERY_EVENTS;
  }

  @Override
  protected RedisEventHandler getHandler() {
    return deliveryEventHandler;
  }
}
