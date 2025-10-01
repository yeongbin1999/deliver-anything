package com.deliveranything.domain.settlement.dto;

import com.deliveranything.domain.settlement.entity.SettlementDetail;
import java.math.BigDecimal;
import java.util.List;

public record UnsettledResponse(
    Long scheduledSettleAmount,
    Integer scheduledTransactionCount
) {

  public static UnsettledResponse from(List<SettlementDetail> settlementDetails) {
    return new UnsettledResponse(
        settlementDetails.stream()
            .map(SettlementDetail::getTargetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .longValue(),
        settlementDetails.size()
    );
  }
}
