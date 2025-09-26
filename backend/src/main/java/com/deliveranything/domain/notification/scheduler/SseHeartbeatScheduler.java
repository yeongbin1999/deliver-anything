package com.deliveranything.domain.notification.scheduler;

import com.deliveranything.domain.notification.repository.EmitterRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final EmitterRepository emitterRepository;

    // 30초마다 실행
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        Map<Long, List<SseEmitter>> allEmitters = emitterRepository.getAllEmitters();
        log.trace("Sending heartbeat to {} profiles.", allEmitters.size());

        allEmitters.forEach((profileId, emitters) -> {
            for (SseEmitter emitter : List.copyOf(emitters)) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("keep-alive"));
                } catch (IOException e) {
                    log.warn("Heartbeat failed for profileId: {}. Removing emitter.", profileId, e);
                    emitterRepository.remove(profileId, emitter);
                }
            }
        });
    }
}
