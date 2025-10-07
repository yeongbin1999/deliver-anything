package com.deliveranything.domain.settlement.dto;

import com.deliveranything.domain.settlement.dto.projection.SettlementProjection;
import com.deliveranything.domain.settlement.entity.SettlementBatch;
import java.math.RoundingMode;
import java.time.LocalDate;

public record SettlementResponse(
    Long totalAmount,
    Long totalPlatformFee,
    Long settledAmount,
    Long transactionCount,
    LocalDate startDate,
    LocalDate endDate
) {

  public static SettlementResponse from(SettlementBatch settlement) {
    return new SettlementResponse(
        settlement.getTargetTotalAmount().longValue(),
        settlement.getTotalPlatformFee().longValue(),
        settlement.getSettledAmount().setScale(0, RoundingMode.CEILING).longValue(),
        settlement.getTransactionCount(),
        settlement.getSettlementDate(),
        settlement.getSettlementDate()
    );
  }

  public static SettlementResponse fromProjection(SettlementProjection sp) {
    return new SettlementResponse(
        sp.targetTotalAmount().longValue(),
        sp.totalPlatformFee().longValue(),
        sp.settledAmount().setScale(0, RoundingMode.CEILING).longValue(),
        sp.transactionCount(),
        sp.minDate(),
        sp.maxDate()
    );
  }
}
