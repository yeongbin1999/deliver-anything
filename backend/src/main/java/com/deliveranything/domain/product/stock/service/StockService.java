package com.deliveranything.domain.product.stock.service;

import com.deliveranything.domain.product.product.repository.ProductRepository;
import com.deliveranything.domain.product.product.service.ProductService;
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
  private final ProductRepository productRepository;
  private final ProductService productService;

  @Transactional(readOnly = true)
  public StockResponse getProductStock(Long productId) {
    productService.findById(productId);

    Stock stock = findById(productId);

    return StockResponse.from(stock);
  }

  @Transactional
  public StockResponse updateProductStock(Long productId, StockUpdateRequest stockUpdateRequest) {
    productService.findById(productId);

    Stock stock = findById(productId);

    stock.setQuantity(stockUpdateRequest.stockChange());

    return StockResponse.from(stock);
  }

  @Transactional(readOnly = true)
  public Stock findById(Long productId) {
    return stockRepository.findById(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
  }
}
