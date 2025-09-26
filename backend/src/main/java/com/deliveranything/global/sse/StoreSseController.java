package com.deliveranything.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/sse")
@RestController
public class StoreSseController {

  private final SseEmitterManager sseEmitterManager;

  // 상점 주인의 구독
  @GetMapping("/{storeId}/subscribe")
  public SseEmitter subscribe(@PathVariable Long storeId) {
    return sseEmitterManager.subscribe("store-" + storeId);
  }

  /**
   * SSE 클라이언트와 서버 사이의 1:1 연결
   * 상점(Store)은 데이터 엔티티이지, 주체가 아님
   * SSE 연결 주체는 항상 User(Client) 이어야 함
   * 클라이언트가 subscribe 호출 → 서버가 userId 기준으로 emitter 등록
   * 여기서 문제 1번
   * 각 프로필을 서브 계정처럼 사용하고 있는데 프로필 Id가 각 프로필마다 존재해 겹침
   *
   */
}