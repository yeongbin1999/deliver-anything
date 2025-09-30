package com.deliveranything.domain.delivery.event.event;

import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.deliveranything.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryStatusSsePublisher {

  private static final String EVENT_NAME = "delivery-status-event";
  private final NotificationService notificationService;

  // 프로필 단위 전체 전송 (해당 프로필의 모든 emitter로 전송)
  public void publish(DeliveryStatusEvent event) {
    if (event.customerProfileId() != null) {
      notificationService.sendToAll(event.customerProfileId(), EVENT_NAME, event);
    }
    if (event.sellerProfileId() != null) {
      notificationService.sendToAll(event.sellerProfileId(), EVENT_NAME, event);
    }
    if (event.riderProfileId() != null) {
      notificationService.sendToAll(event.riderProfileId(), EVENT_NAME, event);
    }
  }
}
