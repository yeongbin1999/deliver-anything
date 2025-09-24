package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.delivery.websocket.RiderWebSocketPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiderEtaConsumer {

  private final RiderWebSocketPublisher webSocketPublisher;

  @KafkaListener(topics = "order-events", groupId = "delivery-service")
  public void consume(RiderNotificationDto dto) {
    webSocketPublisher.publishToRider(dto.riderId(), dto);
  }
}
