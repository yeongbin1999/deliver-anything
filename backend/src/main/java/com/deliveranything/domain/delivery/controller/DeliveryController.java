package com.deliveranything.domain.delivery.controller;

import com.deliveranything.domain.delivery.dto.request.DeliveryAreaRequestDto;
import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.service.DeliveryService;
import com.deliveranything.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  @PatchMapping("/status")
  @Operation(summary = "라이더 토글 전환", description = "라이더 토글 전환으로 상태를 전환합니다.")
  public ResponseEntity<ApiResponse<Void>> updateRiderStatus(
      @RequestBody RiderToggleStatusRequestDto riderStatusRequestDto
  ) {
    deliveryService.updateRiderStatus(riderStatusRequestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/area")
  @Operation(summary = "배달 가능 지역 설정",
      description = "배달 가능 지역을 설정합니다 (현재는 1군데만, 자유로운 형식으로 가능).")
  public ResponseEntity<ApiResponse<Void>> updateDeliveryArea(
      @RequestBody DeliveryAreaRequestDto deliveryAreaRequestDto
  ) {
    deliveryService.updateDeliveryArea(deliveryAreaRequestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
