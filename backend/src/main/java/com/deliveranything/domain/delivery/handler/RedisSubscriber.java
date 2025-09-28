package com.deliveranything.domain.delivery.handler;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.domain.notification.repository.EmitterRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

  //  private final RiderWebSocketPublisher webSocketPublisher;
  private final ObjectMapper objectMapper;
  private final EmitterRepository emitterRepository;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String channel = new String(message.getChannel());
      String body = new String(message.getBody());

      // rider-notifications
      if ("order-events".equals(channel)) {
        RiderNotificationDto dto = objectMapper.readValue(body, RiderNotificationDto.class);
//        webSocketPublisher.publishToRider(dto.riderId(), dto);
        emitterRepository.sendToAll(dto.riderId(), "RIDER_NOTIFICATION", dto);
      }
    } catch (Exception e) {
      throw new CustomException(ErrorCode.REDIS_MESSAGE_PROCESSING_ERROR);
    }
  }
}
