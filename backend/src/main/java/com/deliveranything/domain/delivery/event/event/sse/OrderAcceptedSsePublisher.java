package com.deliveranything.domain.delivery.event.event.sse;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderAcceptedSsePublisher {

  private static final String EVENT_NAME = "order-accepted-event";
  private final NotificationService notificationService;

  // 프로필 단위 전체 전송 (해당 프로필의 모든 emitter로 전송)
  public void publish(List<RiderNotificationDto> events) {
    events.forEach(event ->
        notificationService.sendToAll(Long.parseLong(event.riderId()),
            "RIDER_ASSIGNMENT_NOTIFICATION", event)
    );

  }

}
