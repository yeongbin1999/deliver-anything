package com.deliveranything.domain.notification.service;

import com.deliveranything.domain.notification.entity.Notification;
import com.deliveranything.domain.notification.enums.NotificationType;
import com.deliveranything.domain.notification.repository.EmitterRepository;
import com.deliveranything.domain.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final EmitterRepository emitterRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * 프로필 ID 기준 알림 생성 및 모든 디바이스 전송
   */
  public Notification sendNotification(Long profileId, NotificationType type, String message,
      String data) {
    Notification notification = new Notification();
    notification.setRecipientId(profileId);
    notification.setType(type);
    notification.setMessage(message);
    notification.setData(data);

    notificationRepository.save(notification);

    broadcastToEmitters(profileId, notification, "notification");

    return notification;
  }

  /**
   * 알림 읽음 처리 + 다른 디바이스 동기화
   */
  @Transactional
  public void markAsRead(Long notificationId, Long profileId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    if (notification.getRecipientId().equals(profileId) && !notification.isRead()) {
      notification.setRead(true);

      // 읽음 상태를 다른 디바이스에도 브로드캐스트
      broadcastToEmitters(profileId, notificationId, "notification-read");
    }
  }

  /**
   * 프로필 ID 기준 알림 목록 조회
   */
  public List<Notification> getNotifications(Long profileId, Boolean isRead) {
    if (isRead == null) {
      return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(profileId);
    } else {
      return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(profileId,
          isRead);
    }
  }

  public long getUnreadCount(Long profileId) {
    return notificationRepository.countByRecipientIdAndIsReadFalse(profileId);
  }

  /**
   * 멀티 디바이스 브로드캐스트 공통 메서드
   */
  private void broadcastToEmitters(Long profileId, Object payload, String eventName) {
    List<SseEmitter> emitters = emitterRepository.getAllForProfile(profileId);
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .id(payload instanceof Notification ? ((Notification) payload).getId().toString()
                : payload.toString())
            .name(eventName)
            .data(payload));
      } catch (Exception e) {
        log.warn("SSE send failed for profileId {}: {}. Completing emitter.", profileId,
            e.getMessage());
        // Emitter를 완료시켜 onCompletion 콜백이 호출되도록 함
        emitter.complete();
      }
    }
  }

  /**
   * 해당 profileId의 모든 emitter로 전송
   */
  public void sendToAll(Long profileId, String eventName, Object payload) {
    Map<String, SseEmitter> map = emitterRepository.getAllEmitters().get(profileId);
    if (map == null) {
      return;
    }
    map.forEach((deviceId, emitter) -> {
      try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .id(String.valueOf(System.currentTimeMillis()))
            .data(payload));
      } catch (IOException e) {
        emitter.completeWithError(e);
      }
    });
  }
}