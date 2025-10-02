package com.deliveranything.domain.settlement.service.dto;

import java.math.BigDecimal;

public record SettlementSummary(
    BigDecimal totalTargetAmount,
    BigDecimal totalPlatformFee,
    int transactionCount
) {

}