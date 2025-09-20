package com.deliveranything.domain.order.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    return ResponseEntity.status(CREATED).body(ApiResponse.success(
        orderService.createOrder(인증 객체의 소비자 ID , orderCreateRequest)));
  }

  @GetMapping
  @Operation(summary = "소비자 주문 전체 조회", description = "소비자가 전체 주문 내역을 요청한 경우")
  public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll() {
    return ResponseEntity.ok().body(ApiResponse.success(
        "소비자 전체 주문 내역 조회 성공", orderService.getCustomerOrders(인증 객체의 소비자 ID)));
  }

  @GetMapping("/{orderId}")
  @Operation(summary = "소비자 주문 단일 조회", description = "소비자가 어떤 주문의 상세 정보를 요청한 경우")
  public ResponseEntity<ApiResponse<List<OrderResponse>>> get(@PathVariable Long orderId) {
    return ResponseEntity.ok().body(ApiResponse.success(
        "소비자 주문 단일 조회 성공", orderService.getCustomerOrder(orderId, 인증 객체의 소비자 ID)));
  }
}
