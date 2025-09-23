package com.deliveranything.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentConfirmRequest(
    @NotNull @Size(max = 200) String paymentKey,
    @NotNull @Size(max = 64) String merchantUid,
    @Positive long amount
) {

}
