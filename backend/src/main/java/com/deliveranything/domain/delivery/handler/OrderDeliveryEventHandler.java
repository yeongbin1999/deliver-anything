package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.OrderDeliveryCreatedEvent;
import com.deliveranything.domain.delivery.event.event.RiderEtaPublisher;
import com.deliveranything.domain.delivery.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryEventHandler {

  private final OrderNotificationService orderNotificationService;
  private final RiderEtaPublisher riderEtaPublisher;

  @KafkaListener(topics = "order-events", groupId = "delivery-service")
  public void handleOrderCreated(OrderDeliveryCreatedEvent orderEvent) {
    // Kafka 발행 분리
    orderNotificationService.processOrderEvent(orderEvent)
        .subscribe(riderEtaPublisher::publish);
  }
}
