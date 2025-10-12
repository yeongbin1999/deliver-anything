package com.deliveranything.domain.product.stock.event;

import java.util.List;

public record StockReleasedEvent(
    Long orderId,
    List<StockItemInfo> stockItems
) {

}