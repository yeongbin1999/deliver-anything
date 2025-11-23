package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.DeliveryEventHandler;
import com.deliveranything.domain.order.handler.RedisEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
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
