package com.deliveranything.domain.delivery.event.event.sse;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderAssignmentSsePublisher {

  private static final String EVENT_NAME = "order-events";
  private final NotificationService notificationService;

  // 프로필 단위 전체 전송 (해당 프로필의 모든 emitter로 전송)
  public void publish(RiderNotificationDto event) {
    notificationService.sendToAll(Long.parseLong(event.riderId()), "RIDER_ASSIGNMENT_NOTIFICATION",
        event);
  }

}
