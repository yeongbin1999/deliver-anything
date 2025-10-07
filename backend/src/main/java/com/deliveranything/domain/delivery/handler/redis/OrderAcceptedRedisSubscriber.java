package com.deliveranything.domain.delivery.handler.redis;

import com.deliveranything.domain.delivery.event.event.sse.OrderAcceptedSsePublisher;
import com.deliveranything.domain.delivery.service.OrderNotificationService;
import com.deliveranything.domain.order.event.OrderAcceptedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
// 로드 밸런싱 / 다중 인스턴스 환경 대응하여 Redis Pub/Sub 구축
public class OrderAcceptedRedisSubscriber {

  private static final String CHANNEL = "order-accepted-event";
  private final ObjectMapper objectMapper;
  private final ReactiveStringRedisTemplate redisTemplate;
  private final OrderAcceptedSsePublisher orderAssignmentSsePublisher;
  private final OrderNotificationService orderNotificationService;

  @PostConstruct // 애플리케이션 구동 시 자동 실행
  public void subscribeToOrderChannel() {
    redisTemplate.listenToChannel(CHANNEL) // Redis 채널 구독 시작
        .map(message -> {
          try {
            return objectMapper.readValue(message.getMessage(), OrderAcceptedEvent.class);
          } catch (JsonProcessingException e) {
            log.error("Failed to parse Redis message", e);
            return null;
          }
        }) // 수신된 메시지 내용 추출
        .filter(Objects::nonNull) // null 제거
        .flatMap(orderNotificationService::processOrderEvent) // 비즈니스 처리
        .doOnNext(orderAssignmentSsePublisher::publish) // SSE로 전송
        .onErrorResume(e -> {
          log.error("RedisSubscriber error: {}", e.getMessage());
          return Mono.empty(); // 끊김 방지
        })
        .subscribe(); // 실제 구독 실행
  }
}
