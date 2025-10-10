package com.deliveranything.domain.notification.subscriber.delivery;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.notification.enums.NotificationMessage;
import com.deliveranything.domain.notification.enums.NotificationType;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderAcceptedNotifier {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;

  // 프로필 단위 전체 전송 (해당 프로필의 모든 emitter로 전송)
  public void publish(List<RiderNotificationDto> events) {
    events.forEach(event ->
        {
          try {
            notificationService.sendNotification(
                Long.parseLong(event.riderId()),
                NotificationType.RIDER_ACCEPTED_ORDER,
                NotificationMessage.RIDER_ACCEPTED_ORDER.getMessage(),
                objectMapper.writeValueAsString(event));
          } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.SSE_SUBSCRIBE_UNAVAILABLE);
          }
        }
    );

  }

}
