package com.deliveranything.domain.notification.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class EmitterRepository {

  private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public void save(Long profileId, SseEmitter emitter) {
    emitters.computeIfAbsent(profileId, k -> new ArrayList<>()).add(emitter);
  }

  public List<SseEmitter> get(Long profileId) {
    return emitters.getOrDefault(profileId, new ArrayList<>());
  }

  public void remove(Long profileId, SseEmitter emitter) {
    List<SseEmitter> list = emitters.get(profileId);
    if(list != null) {
      list.remove(emitter);
      if(list.isEmpty()) emitters.remove(profileId);
    }
  }

  public Map<Long, List<SseEmitter>> getAllEmitters() {
    return emitters;
  }
}