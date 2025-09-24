package com.deliveranything.domain.delivery.websocket;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RiderWebSocketPublisher {

  private final SimpMessagingTemplate messagingTemplate;

  public RiderWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void publishToRider(String riderId, RiderNotificationDto dto) {
    String destination = "/topic/rider/" + riderId;
    messagingTemplate.convertAndSend(destination, dto);
  }
}
