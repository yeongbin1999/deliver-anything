package com.deliveranything.domain.settlement.controller;

import com.deliveranything.domain.settlement.dto.SettlementResponse;
import com.deliveranything.domain.settlement.service.SettlementBatchService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
@RestController
public class SettlementController {

  private final SettlementBatchService settlementBatchService;

  @GetMapping
  @Operation(summary = "정산 내역 조회", description = "판매자/배달원이 정산 내역을 요청한 경우")
  public ResponseEntity<ApiResponse<List<SettlementResponse>>> getAll(
      @AuthenticationPrincipal SecurityUser user
  ) {
    if (user.isSellerActive() || user.isRiderActive()) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("전체 정산 내역 조회 성공",
        settlementBatchService.getSettlements(user.getId(),
            user.getCurrentActiveProfile().getType())));
  }

  @GetMapping("/{settlementId}")
  @Operation(summary = "정산 단일 조회", description = "판매자/배달원이 어떤 주문의 상세 정보를 요청한 경우")
  public ResponseEntity<ApiResponse<SettlementResponse>> get(
      @AuthenticationPrincipal SecurityUser user,
      @PathVariable Long settlementId
  ) {
    if (user.isSellerActive() || user.isRiderActive()) {
      throw new CustomException(ErrorCode.PROFILE_TYPE_FORBIDDEN);
    }

    return ResponseEntity.ok().body(ApiResponse.success("정산 단일 조회 성공",
        settlementBatchService.getSettlement(settlementId)));
  }
}
