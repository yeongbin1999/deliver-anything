package com.deliveranything.domain.delivery.websocket;

import com.deliveranything.domain.delivery.dto.RiderLocationDto;
import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiderWebSocketPublisher {

  private final SimpMessagingTemplate messagingTemplate;

  // 여기 SSE 변경 보류
  public void publishToRider(String riderId, RiderNotificationDto dto) {
    String destination = "/topic/rider/" + riderId;
    messagingTemplate.convertAndSend(destination, dto);
  }

  public void publishLocation(RiderLocationDto location) {
    String destination = "/topic/rider/location";
    messagingTemplate.convertAndSend(destination, location);
  }
}
