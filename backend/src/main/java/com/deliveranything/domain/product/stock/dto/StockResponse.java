package com.deliveranything.domain.product.stock.dto;

import com.deliveranything.domain.product.stock.entity.Stock;

public record StockResponse(
    Integer stockQuantity
) {

  public static StockResponse from(Stock stock) {
    return new StockResponse(
        stock.getQuantity()
    );
  }
}
