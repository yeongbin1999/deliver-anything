package com.deliveranything.domain.delivery.event.event;

import com.deliveranything.domain.delivery.event.dto.RiderNotificationDto;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void publish(List<RiderNotificationDto> dtoList) {
    dtoList.forEach(dto -> {
      try {
        String channel = "order-events";
        String message = objectMapper.writeValueAsString(dto);
        redisTemplate.convertAndSend(channel, message);
      } catch (Exception e) {
        throw new CustomException(ErrorCode.REDIS_MESSAGE_PROCESSING_ERROR);
      }
    });
  }
}
