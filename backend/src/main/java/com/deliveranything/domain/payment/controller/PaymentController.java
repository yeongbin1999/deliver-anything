package com.deliveranything.domain.payment.controller;

import com.deliveranything.domain.payment.service.PaymentService;
import com.deliveranything.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
