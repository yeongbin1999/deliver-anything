package com.deliveranything.domain.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementProjection(
    BigDecimal targetTotalAmount,
    BigDecimal totalPlatformFee,
    BigDecimal settledAmount,
    Long transactionCount,
    LocalDate minDate,
    LocalDate maxDate
) {

}
