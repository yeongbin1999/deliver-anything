package com.deliveranything.domain.store.controller;

import com.deliveranything.global.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RequestMapping("/api/v1/stores/sse")
@RestController
public class StoreOrderSseController {

  private final SseEmitterManager sseEmitterManager;

  // 상점 주인의 구독
  @GetMapping("/{storeId}/subscribe")
  public SseEmitter subscribe(@PathVariable Long storeId) {
    return sseEmitterManager.subscribe("store-" + storeId);
  }
}
