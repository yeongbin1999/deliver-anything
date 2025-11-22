package com.deliveranything.domain.notification.subscriber.delivery;

import com.deliveranything.domain.delivery.event.dto.DeliveryOfferFailedEvent;
import com.deliveranything.domain.notification.enums.NotificationMessage;
import com.deliveranything.domain.notification.enums.NotificationType;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryOfferFailedNotifier {

  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  public void publish(DeliveryOfferFailedEvent event) {
    try {
      if (event.order() != null) {
        notificationService.sendNotification(
            event.order().sellerId(),
            NotificationType.RIDER_OFFER_FAILED,
            NotificationMessage.RIDER_OFFER_FAILED.getMessage(),
            objectMapper.writeValueAsString(event));
      }
    } catch (Exception e) {
      throw new CustomException(ErrorCode.SSE_SUBSCRIBE_UNAVAILABLE);
    }
  }
}
