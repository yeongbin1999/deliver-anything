package com.deliveranything.domain.order.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.deliveranything.domain.order.dto.OrderCancelRequest;
import com.deliveranything.domain.order.dto.OrderCreateRequest;
import com.deliveranything.domain.order.dto.OrderPayRequest;
import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @Operation(summary = "주문 생성", description = "소비자가 상점에 주문을 요청한 경우")
  public ResponseEntity<ApiResponse<OrderResponse>> create(
      @AuthenticationPrincipal SecurityUser user,
      @Valid @RequestBody OrderCreateRequest orderCreateRequest
  ) {
    if (!user.hasActiveProfile(ProfileType.CUSTOMER)) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.status(CREATED).body(ApiResponse.success(
        orderService.createOrder(user.getCurrentActiveProfile().getId(), orderCreateRequest)));
  }

  @GetMapping
  @Operation(summary = "주문 내역 조회", description = "소비자가 주문 내역을 요청한 경우")
  public ResponseEntity<ApiResponse<CursorPageResponse<OrderResponse>>> getAll(
      @AuthenticationPrincipal SecurityUser user,
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (!user.hasActiveProfile(ProfileType.CUSTOMER)) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("소비자 전체 주문 내역 조회 성공",
        orderService.getCustomerOrdersByCursor(user.getCurrentActiveProfile().getId(), cursor,
            size)));
  }

  @GetMapping("/{orderId}")
  @Operation(summary = "주문 단일 조회", description = "소비자가 어떤 주문의 상세 정보를 요청한 경우")
  public ResponseEntity<ApiResponse<OrderResponse>> get(
      @AuthenticationPrincipal SecurityUser user,
      @PathVariable Long orderId
  ) {
    if (!user.hasActiveProfile(ProfileType.CUSTOMER)) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("소비자 주문 단일 조회 성공",
        orderService.getCustomerOrder(orderId, user.getCurrentActiveProfile().getId())));
  }

  @PostMapping("/{merchantUid}/pay")
  @Operation(summary = "주문 결제", description = "소비자가 생성한 주문의 결제 시도")
  public ResponseEntity<ApiResponse<OrderResponse>> pay(
      @AuthenticationPrincipal SecurityUser user,
      @PathVariable String merchantUid,
      @Valid @RequestBody OrderPayRequest orderPayRequest
  ) {
    if (!user.hasActiveProfile(ProfileType.CUSTOMER)) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("소비자 결제 승인 성공",
        orderService.payOrder(merchantUid, orderPayRequest.paymentKey())));
  }

  @PostMapping("/{orderId}/cancel")
  @Operation(summary = "주문 취소", description = "소비자가 상점에서 주문 수락 전인 주문을 취소하는 경우")
  public ResponseEntity<ApiResponse<OrderResponse>> cancel(
      @AuthenticationPrincipal SecurityUser user,
      @PathVariable Long orderId,
      @RequestBody OrderCancelRequest orderCancelRequest
  ) {
    if (!user.hasActiveProfile(ProfileType.CUSTOMER)) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("소비자 주문 취소 성공",
        orderService.cancelOrder(orderId, orderCancelRequest.cancelReason())));
  }
}
