package com.deliveranything.domain.delivery.event.event.sse;

import com.deliveranything.domain.delivery.event.dto.OrderStatusUpdateEvent;
import com.deliveranything.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryStatusSsePublisher {

  private static final String EVENT_NAME = "order-delivery-status";
  private final NotificationService notificationService;

  // 프로필 단위 전체 전송 (해당 프로필의 모든 emitter로 전송)
  public void publish(Long profileId, OrderStatusUpdateEvent event) {
    notificationService.sendToAll(profileId, "RIDER_ASSIGNMENT_NOTIFICATION",
        event);
  }
}
