package com.deliveranything.domain.product.stock.service;

import com.deliveranything.domain.product.stock.dto.StockResponse;
import com.deliveranything.domain.product.stock.entity.Stock;
import com.deliveranything.domain.product.stock.repository.StockRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockTransactionalService stockTransactionalService;
  private final StockRepository stockRepository;

  private static final int MAX_RETRIES = 3;

  @Transactional(readOnly = true)
  public StockResponse getProductStock(Long productId) {
    Stock stock = stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    return StockResponse.from(stock);
  }

  // 주문용: 증감
  public StockResponse changeStockByOrder(Long productId, int change) {
    int retries = 0;
    while (true) {
      try {
        return stockTransactionalService.changeStockTransactional(productId, change);
      } catch (OptimisticLockException e) {
        if (++retries >= MAX_RETRIES) {
          throw new CustomException(ErrorCode.STOCK_CHANGE_CONFLICT);
        }
      }
    }
  }

  // 관리자용: 직접 세팅
  public StockResponse updateStockByAdmin(Long productId, int newQuantity) {
    int retries = 0;
    while (true) {
      try {
        return stockTransactionalService.setStockTransactional(productId, newQuantity);
      } catch (OptimisticLockException e) {
        if (++retries >= MAX_RETRIES) {
          throw new CustomException(ErrorCode.STOCK_CHANGE_CONFLICT);
        }
      }
    }
  }
}