package com.deliveranything.domain.payment.controller;

import com.deliveranything.domain.payment.dto.PaymentConfirmRequest;
import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController {

  private final PaymentService paymentService;

  @PatchMapping("/{paymentId}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable("paymentId") Long paymentId) {
    paymentService.cancelPayment(paymentId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @PostMapping("/{paymentId}/verify")
  public ResponseEntity<ApiResponse<Void>> verify(
      @PathVariable("paymentId") Long paymentId,
      @Valid @RequestBody PaymentConfirmRequest request
  ) {
    paymentService.confirmPayment(paymentId, request);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
