package com.deliveranything.domain.product.stock.event;

import java.util.List;

public record StockReservedFailedEvent(
    Long orderId,
    List<StockItemInfo> stockItems,
    String reason
) {

}