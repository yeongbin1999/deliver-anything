package com.deliveranything.domain.order.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @Operation(summary = "주문 생성", description = "소비자가 상점에 주문을 요청한 경우")
  public ResponseEntity<ApiResponse<OrderResponse>> create(
      @Valid @RequestBody OrderCreateRequest orderCreateRequest
  ) {
    return ResponseEntity.status(CREATED)
        .body(ApiResponse.success(orderService.createOrder(orderCreateRequest)));
  }
}
