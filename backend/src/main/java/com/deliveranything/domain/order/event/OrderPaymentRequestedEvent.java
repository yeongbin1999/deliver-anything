package com.deliveranything.domain.order.event;

public record OrderPaymentRequestedEvent(
    String paymentKey,
    String merchantUid,
    long amount
) {

}
