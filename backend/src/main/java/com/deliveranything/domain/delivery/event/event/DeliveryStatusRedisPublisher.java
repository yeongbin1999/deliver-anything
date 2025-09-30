package com.deliveranything.domain.delivery.event.event;

import com.deliveranything.domain.delivery.event.dto.DeliveryStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryStatusRedisPublisher {

  private static final String CHANNEL = "delivery-status-events";
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void publish(DeliveryStatusEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      redisTemplate.convertAndSend(CHANNEL, payload);
    } catch (Exception e) {
      // TODO: 로깅
    }
  }
}
