package com.deliveranything.domain.order.subscriber;

import com.deliveranything.domain.order.handler.StockEventHandler;
import com.deliveranything.global.enums.RedisTopicPattern;
import com.deliveranything.global.event.AbstractRedisEventSubscriber;
import com.deliveranything.global.event.RedisEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventSubscriber extends AbstractRedisEventSubscriber {

  private final StockEventHandler stockEventHandler;

  @Override
  protected RedisTopicPattern getTopicPattern() {
    return RedisTopicPattern.STOCK_EVENTS;
  }

  @Override
  protected RedisEventHandler getHandler() {
    return stockEventHandler;
  }
}