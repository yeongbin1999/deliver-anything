package com.deliveranything.domain.product.stock.event;

public record StockItemInfo(
    Long productId,
    int quantity
) {}