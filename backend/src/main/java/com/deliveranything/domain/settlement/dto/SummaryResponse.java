package com.deliveranything.domain.settlement.dto;

import com.deliveranything.domain.settlement.dto.projection.SettlementSummaryProjection;
import java.math.BigDecimal;

public record SummaryResponse(
    Long totalTransactionCount,
    Long weeklyTransactionCount,
    BigDecimal weeklySettledAmount,
    BigDecimal monthlySettledAmount,
    Long totalSettledAmount,
    Long scheduledSettleAmount
) {

  public static SummaryResponse fromSettledAndUnsettled(
      SettlementSummaryProjection ssp,
      UnsettledResponse ur
  ) {
    return new SummaryResponse(
        ssp.totalTransactionCount() + ur.scheduledTransactionCount(),
        ssp.weeklyTransactionCount() + ur.scheduledTransactionCount(),
        ssp.weeklySettledAmount(),
        ssp.monthlySettledAmount(),
        ssp.totalSettledAmount().longValue(),
        ur.scheduledSettleAmount()
    );
  }
}
