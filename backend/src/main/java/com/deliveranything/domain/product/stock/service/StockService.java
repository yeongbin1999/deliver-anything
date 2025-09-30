package com.deliveranything.domain.product.stock.service;

import com.deliveranything.domain.product.stock.dto.StockResponse;
import com.deliveranything.domain.product.stock.dto.StockUpdateRequest;
import com.deliveranything.domain.product.stock.entity.Stock;
import com.deliveranything.domain.product.stock.repository.StockRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;

  @Transactional(readOnly = true)
  public StockResponse getProductStock(Long productId) {
    Stock stock = stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    return StockResponse.from(stock);
  }

  @Transactional
  public StockResponse updateProductStock(Long productId, StockUpdateRequest request) {
    Stock stock = stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    stock.setQuantity(request.stockChange());

    return StockResponse.from(stock);
  }
}
