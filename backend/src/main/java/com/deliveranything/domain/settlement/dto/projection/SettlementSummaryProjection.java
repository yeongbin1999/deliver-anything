package com.deliveranything.domain.settlement.dto.projection;

import java.math.BigDecimal;

public record SettlementSummaryProjection(
    Long totalTransactionCount,
    Long weeklyTransactionCount,
    Long weeklySettledAmount,
    Long monthlySettledAmount,
    BigDecimal totalSettledAmount
) {

}
