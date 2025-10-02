package com.deliveranything.domain.settlement.dto;

import com.deliveranything.domain.settlement.entity.SettlementBatch;
import java.math.RoundingMode;
import java.time.LocalDate;

public record SettlementResponse(
    Long settlementId,
    Long totalAmount,
    Long totalPlatformFee,
    Long settledAmount,
    Integer transactionCount,
    LocalDate settlementDate
) {

  public static SettlementResponse from(SettlementBatch settlement) {
    return new SettlementResponse(
        settlement.getId(),
        settlement.getTargetTotalAmount().longValue(),
        settlement.getTotalPlatformFee().longValue(),
        settlement.getSettledAmount().setScale(0, RoundingMode.CEILING).longValue(),
        settlement.getTransactionCount(),
        settlement.getSettlementDate()
    );
  }
}
