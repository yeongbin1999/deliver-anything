package com.deliveranything.domain.product.stock.event;

import java.util.List;

public record StockCommittedEvent(
    Long orderId,
    List<StockItemInfo> stockItems
) {

}