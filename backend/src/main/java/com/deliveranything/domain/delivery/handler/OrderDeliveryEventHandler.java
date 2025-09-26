package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderDeliveryCreatedEvent;
import com.deliveranything.domain.delivery.event.event.RedisPublisher;
import com.deliveranything.domain.delivery.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryEventHandler {

  private final OrderNotificationService orderNotificationService;
  private final RedisPublisher redisPublisher; // Kafka → Redis 변경

  @EventListener // @KafkaListener → @EventListener로 변경
  public void handleOrderCreated(OrderDeliveryCreatedEvent orderEvent) {
    orderNotificationService.processOrderEvent(orderEvent)
        .subscribe(redisPublisher::publish); // Kafka → Redis 변경
  }
}
