package com.deliveranything.domain.order.event;

public record OrderPaymentRequestedEvent(
    Long orderId,
    String paymentKey,
    String merchantUid,
    Long amount
) {

}
