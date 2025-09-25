package com.deliveranything.global.sse;

import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(String clientId) {
    SseEmitter emitter = new SseEmitter(Duration.ofMinutes(10).toMillis());
    emitters.put(clientId, emitter);

    emitter.onCompletion(() -> emitters.remove(clientId));
    emitter.onTimeout(() -> emitters.remove(clientId));
    emitter.onError((e) -> emitters.remove(clientId));

    // 최초의 연결 확인 (프론트 측에서 알 수 있게)
    try {
      emitter.send(SseEmitter.event()
          .name("CONNECT CHECK")
          .data("connected"));
    } catch (IOException e) {
      emitters.remove(clientId);
      throw new CustomException(ErrorCode.SSE_SUBSCRIBE_UNAVAILABLE);
    }

    return emitter;
  }

  // 실시간 업데이트 필요할 시 클라이언트에게 전달
  public void sendToClient(String clientId, String eventName, Object data) {
    SseEmitter emitter = emitters.get(clientId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(data));
      } catch (IOException e) {
        emitters.remove(clientId);
        // TODO: send를 못하게 됐을떄 emiiter가 죽었다고 없애면 서버가 전송하려 했던 데이터는 유실된다. 후에 안정성 고안할 것
      }
    }
  }
}
