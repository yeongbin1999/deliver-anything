package com.deliveranything.domain.product.stock.controller;

import com.deliveranything.domain.product.stock.dto.StockResponse;
import com.deliveranything.domain.product.stock.dto.StockUpdateRequest;
import com.deliveranything.domain.product.stock.service.StockService;
import com.deliveranything.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StockController {

  private final StockService stockService;

  @GetMapping("/products/{productId}/stock")
  public ResponseEntity<ApiResponse<StockResponse>> getProductStock(
      @PathVariable Long productId
  ) {

    StockResponse stockResponse = stockService.getProductStock(productId);

    return ResponseEntity.ok(ApiResponse.success(stockResponse));
  }

  @PutMapping("/products/{productId}/stock")
  public ResponseEntity<ApiResponse<Void>> updateProductStock(
      @PathVariable Long productId,
      @Valid @RequestBody StockUpdateRequest stockUpdateRequest) {

    StockResponse stockResponse = stockService.updateProductStock(productId, stockUpdateRequest);

    return ResponseEntity.ok(ApiResponse.success());
  }
}