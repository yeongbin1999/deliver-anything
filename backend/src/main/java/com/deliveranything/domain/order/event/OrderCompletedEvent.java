package com.deliveranything.domain.order.event;

import java.math.BigDecimal;

public record OrderCompletedEvent(
    Long orderId,
    Long riderProfileId,
    Long sellerProfileId,
    BigDecimal storePrice,
    BigDecimal deliveryPrice
) {

}
