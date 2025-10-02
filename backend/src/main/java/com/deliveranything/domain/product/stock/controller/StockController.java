package com.deliveranything.domain.product.stock.controller;

import com.deliveranything.domain.product.stock.dto.StockResponse;
import com.deliveranything.domain.product.stock.dto.StockUpdateRequest;
import com.deliveranything.domain.product.stock.service.StockService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.security.auth.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상품 재고 관련 API", description = "상품 재고 관련 API입니다.")
@RestController
@RequestMapping("/api/v1/stores/{storeId}/products/{productId}/stock")
@RequiredArgsConstructor
public class StockController {

  private final StockService stockService;

  @Operation(summary = "상품 재고 조회", description = "특정 상품의 재고 정보를 조회합니다.")
  @GetMapping
  @PreAuthorize("@profileSecurity.isSeller(#securityUser) and @storeSecurity.isOwner(#storeId, #securityUser)")
  public ResponseEntity<ApiResponse<StockResponse>> getProductStock(
      @Parameter(description = "상점 ID") @PathVariable Long storeId,
      @Parameter(description = "재고를 조회할 상품 ID") @PathVariable Long productId,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    return ResponseEntity.ok(ApiResponse.success(stockService.getProductStock(storeId, productId)));
  }

  @Operation(summary = "상품 재고 수정", description = "특정 상품의 재고를 수정합니다.")
  @PutMapping
  @PreAuthorize("@profileSecurity.isSeller(#securityUser) and @storeSecurity.isOwner(#storeId, #securityUser)")
  public ResponseEntity<ApiResponse<StockResponse>> updateProductStock(
      @Parameter(description = "상점 ID") @PathVariable Long storeId,
      @Parameter(description = "재고를 수정할 상품 ID") @PathVariable Long productId,
      @Valid @RequestBody StockUpdateRequest request,
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    return ResponseEntity.ok(ApiResponse.success(stockService.updateStockByAdmin(storeId, productId, request.stockChange())));
  }
}