package com.deliveranything.domain.notification.controller;

import com.deliveranything.domain.notification.entity.Notification;
import com.deliveranything.domain.notification.repository.EmitterRepository;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림 관련 API", description = "알림 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final EmitterRepository emitterRepository;

  @Operation(summary = "SSE 구독", description = "SSE를 통해 실시간 알림을 구독합니다. 각 기기별로 고유한 deviceId를 헤더(X-Device-ID)에 담아 요청해야 합니다.")
  @GetMapping("/stream")
  public SseEmitter subscribe(
      @Parameter(description = "구독할 사용자의 프로필 ID") @RequestParam Long profileId,
      @Parameter(description = "구독하는 기기의 고유 ID", required = true, in = ParameterIn.HEADER)
      @RequestHeader("X-Device-ID") String deviceId
      // @AuthenticationPrincipal CustomUserDetails userDetails <-- 추후 적용
  ) {
    SseEmitter emitter = new SseEmitter(60 * 1000L);
    emitterRepository.save(profileId, deviceId, emitter);

    // 연결 종료 시 Emitter 제거
    emitter.onCompletion(() -> emitterRepository.remove(profileId, deviceId));
    emitter.onTimeout(() -> emitterRepository.remove(profileId, deviceId));

    // 최초 연결 확인 이벤트 전송
    try {
      emitter.send(
          SseEmitter.event().name("connect").data("SSE connected with deviceId: " + deviceId));
    } catch (Exception e) {
      emitterRepository.remove(profileId, deviceId);
    }

    return emitter;
  }

  @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다. isRead 파라미터로 읽음/안읽음 필터링이 가능합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
      @Parameter(description = "조회할 사용자의 프로필 ID") @RequestParam Long profileId,
      @Parameter(description = "읽음 상태 필터 (true: 읽음, false: 안읽음, 미포함: 전체)") @RequestParam(required = false) Boolean isRead
      // @AuthenticationPrincipal CustomUserDetails userDetails <-- 추후 적용
  ) {
    return ResponseEntity.ok(
        ApiResponse.success(notificationService.getNotifications(profileId, isRead)));
  }

  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음으로 표시합니다.")
  @PostMapping("/{id}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @Parameter(description = "읽음 처리할 알림의 ID") @PathVariable Long id,
      @Parameter(description = "사용자 프로필 ID") @RequestParam Long profileId
      // @AuthenticationPrincipal CustomUserDetails userDetails <-- 추후 적용
  ) {
    notificationService.markAsRead(id, profileId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(summary = "읽지 않은 알림 수 조회", description = "사용자의 읽지 않은 알림 수를 조회합니다.")
  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(
      @Parameter(description = "사용자 프로필 ID") @RequestParam Long profileId
      // @AuthenticationPrincipal CustomUserDetails userDetails <-- 추후 적용
  ) {
    return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(profileId)));
  }

  @Operation(summary = "라이더 주문 알림 SSE 구독", description = "라이더가 주문 알림을 실시간으로 구독합니다.")
  @GetMapping("/riders/{riderId}/stream")
  public SseEmitter subscribeRiderNotifications(
      @Parameter(description = "라이더 ID") @PathVariable String riderId,
      @Parameter(description = "구독하는 기기의 고유 ID", required = true, in = ParameterIn.HEADER)
      @RequestHeader("X-Device-ID") String deviceId
  ) {
    // 라이더 ID를 Long으로 변환 (실제 구현에서는 라이더 프로필 ID를 사용)
    Long profileId = Long.parseLong(riderId);

    SseEmitter emitter = new SseEmitter(60 * 1000L);
    emitterRepository.save(profileId, deviceId, emitter);

    // 연결 종료 시 Emitter 제거
    emitter.onCompletion(() -> emitterRepository.remove(profileId, deviceId));
    emitter.onTimeout(() -> emitterRepository.remove(profileId, deviceId));

    // 최초 연결 확인 이벤트 전송
    try {
      emitter.send(SseEmitter.event().name("connect")
          .data("Rider SSE connected with deviceId: " + deviceId));
    } catch (Exception e) {
      emitterRepository.remove(profileId, deviceId);
    }

    return emitter;
  }

  @Operation(summary = "라이더 주문 수락/거절 SSE 구독", description = "라이더가 주문 수락/거절 결정을 실시간으로 구독합니다.")
  @GetMapping("/riders/{riderId}/decisions")
  public SseEmitter subscribeRiderDecisions(
      @Parameter(description = "라이더 ID") @PathVariable String riderId,
      @Parameter(description = "구독하는 기기의 고유 ID", required = true, in = ParameterIn.HEADER)
      @RequestHeader("X-Device-ID") String deviceId
  ) {
    // 라이더 ID를 Long으로 변환 (실제 구현에서는 라이더 프로필 ID를 사용)
    Long profileId = Long.parseLong(riderId);

    SseEmitter emitter = new SseEmitter(60 * 1000L);
    emitterRepository.save(profileId, deviceId, emitter);

    // 연결 종료 시 Emitter 제거
    emitter.onCompletion(() -> emitterRepository.remove(profileId, deviceId));
    emitter.onTimeout(() -> emitterRepository.remove(profileId, deviceId));

    // 최초 연결 확인 이벤트 전송
    try {
      emitter.send(SseEmitter.event().name("connect")
          .data("Rider SSE connected with deviceId: " + deviceId));
    } catch (Exception e) {
      emitterRepository.remove(profileId, deviceId);
    }

    return emitter;
  }
}