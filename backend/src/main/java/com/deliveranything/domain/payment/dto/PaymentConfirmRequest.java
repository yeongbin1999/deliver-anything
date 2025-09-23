package com.deliveranything.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
    @NotNull String paymentKey,
    @NotNull String merchantUid,
    long amount
) {

}
