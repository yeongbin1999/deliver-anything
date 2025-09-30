package com.deliveranything.domain.product.stock.service;

import com.deliveranything.domain.product.stock.dto.StockResponse;
import com.deliveranything.domain.product.stock.entity.Stock;
import com.deliveranything.domain.product.stock.repository.StockRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockTransactionalService {

  private final StockRepository stockRepository;

  // 주문용: 재고 증감
  @Transactional
  public StockResponse changeStockTransactional(Long productId, int change) {
    Stock stock = stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    if (change > 0) stock.increaseQuantity(change);
    else stock.decreaseQuantity(-change);

    return StockResponse.from(stock);
  }

  // 관리자용: 재고 직접 세팅
  @Transactional
  public StockResponse setStockTransactional(Long productId, int newQuantity) {
    Stock stock = stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    stock.setQuantity(newQuantity);

    return StockResponse.from(stock);
  }
}