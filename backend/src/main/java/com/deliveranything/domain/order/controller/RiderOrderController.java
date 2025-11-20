package com.deliveranything.domain.order.controller;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.dto.OrderRiderAcceptRequest;
import com.deliveranything.domain.order.service.RiderOrderService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.security.auth.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/rider/orders")
@RestController
@Tag(name = "배달원 주문 API", description = "라이더의 주문처리 관련 API입니다.")
public class RiderOrderController {

  private final RiderOrderService riderOrderService;

  @GetMapping("/{orderId}")
  @Operation(summary = "주문 단일 조회", description = "라이더가 어떤 주문의 상세 정보를 요청한 경우")
  @PreAuthorize("@profileSecurity.isRider(#securityUser)")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
      @AuthenticationPrincipal SecurityUser securityUser,
      @PathVariable Long orderId
  ) {
    return ResponseEntity.ok().body(ApiResponse.success("소비자 주문 단일 조회 성공",
        riderOrderService.getOrderResponse(orderId)));
  }

  @PatchMapping("/{orderId}/accept")
  @Operation(summary = "주문 수락", description = "라이더가 제안된 주문을 수락한 경우")
  @PreAuthorize("@profileSecurity.isRider(#securityUser)")
  public ResponseEntity<ApiResponse<String>> acceptOrder(
      @AuthenticationPrincipal SecurityUser securityUser,
      @PathVariable Long orderId,
      @Valid @RequestBody OrderRiderAcceptRequest orderRiderAcceptRequest
  ) {
    riderOrderService.acceptOrder(
        orderId,
        securityUser.getCurrentActiveProfileIdSafe(),
        orderRiderAcceptRequest
    );

    return ResponseEntity.ok().body(ApiResponse.success("주문 수락 요청 처리중입니다."));
  }
}
