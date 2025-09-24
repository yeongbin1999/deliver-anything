package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.RiderEtaEvent;
import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.delivery.websocket.RiderWebSocketPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RiderEtaConsumer {

  private final RiderWebSocketPublisher webSocketPublisher;

  public RiderEtaConsumer(RiderWebSocketPublisher webSocketPublisher) {
    this.webSocketPublisher = webSocketPublisher;
  }

  @KafkaListener(topics = "order-events", groupId = "delivery-service")
  public void consume(RiderEtaEvent event) {
    for (RiderNotificationDto dto : event.notifications()) {
      webSocketPublisher.publishToRider(dto.riderId(), dto);
    }
  }
}
