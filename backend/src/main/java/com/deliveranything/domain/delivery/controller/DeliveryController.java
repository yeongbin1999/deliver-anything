package com.deliveranything.domain.delivery.controller;

import com.deliveranything.domain.delivery.dto.RiderToggleStatusRequestDto;
import com.deliveranything.domain.delivery.service.DeliveryService;
import com.deliveranything.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  @PatchMapping("/status")
  public ResponseEntity<ApiResponse<Void>> updateRiderStatus(
      @RequestBody RiderToggleStatusRequestDto riderStatusRequestDto
  ) {
    deliveryService.updateRiderStatus(riderStatusRequestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
