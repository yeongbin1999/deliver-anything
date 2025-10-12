package com.deliveranything.domain.product.stock.event;

import java.util.List;

public record StockReservedEvent(
    Long orderId,
    List<StockItemInfo> stockItems
) {

}