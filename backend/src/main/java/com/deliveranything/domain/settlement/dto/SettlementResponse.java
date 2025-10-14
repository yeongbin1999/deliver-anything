package com.deliveranything.domain.settlement.dto;

import com.deliveranything.domain.settlement.dto.projection.SettlementProjection;
import com.deliveranything.domain.settlement.entity.SettlementBatch;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.time.LocalDate;

public record SettlementResponse(
    Long totalAmount,
    Long totalPlatformFee,
    Long settledAmount,
    Integer transactionCount,
    LocalDate startDate,
    LocalDate endDate
) {

  public static SettlementResponse from(SettlementBatch settlement) {
    return new SettlementResponse(
        settlement.getTargetTotalAmount(),
        settlement.getTotalPlatformFee(),
        settlement.getSettledAmount(),
        settlement.getTransactionCount(),
        settlement.getSettlementDate(),
        settlement.getSettlementDate()
    );
  }

  public static SettlementResponse fromProjection(SettlementProjection sp) {
    if (sp.transactionCount() == null) {
      throw new CustomException(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND);
    }

    return new SettlementResponse(
        sp.targetTotalAmount(),
        sp.totalPlatformFee(),
        sp.settledAmount(),
        sp.transactionCount().intValue(),
        sp.minDate(),
        sp.maxDate()
    );
  }
}
