package com.deliveranything.domain.product.stock.event;

import java.util.List;

public record StockReplenishedEvent(
    Long orderId,
    List<StockItemInfo> stockItems
) {

}