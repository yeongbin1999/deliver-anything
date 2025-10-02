package com.deliveranything.domain.notification.handler;

import com.deliveranything.domain.notification.repository.EmitterRepository;
import com.deliveranything.domain.user.user.event.UserLoggedOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final EmitterRepository emitterRepository;

  @EventListener
  public void handleUserLogout(UserLoggedOutEvent event) {
    Long profileId = event.profileId();
    String deviceId = event.deviceId();
    log.info(
        "Handling user logout event for profileId: {}, deviceId: {}. Attempting to terminate specific SSE connection.",
        profileId, deviceId);

    SseEmitter emitter = emitterRepository.get(profileId, deviceId);

    if (emitter != null) {
      try {
        emitter.complete();
        log.info("SSE connection for profileId: {}, deviceId: {} successfully completed.",
            profileId, deviceId);
      } catch (Exception e) {
        log.warn("Error while completing SseEmitter for profileId: {}, deviceId: {}. Message: {}",
            profileId, deviceId, e.getMessage());
        emitterRepository.remove(profileId, deviceId);
      }
    } else {
      log.info("No active SSE emitter found for profileId: {}, deviceId: {} to terminate.",
          profileId, deviceId);
    }
  }
}
