package com.deliveranything.domain.delivery.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record DeliveredSummaryResponseDto(
    Long thisWeekDeliveredCount,
    Long totalDeliveryCharges,
    Integer waitingSettlementAmount,
    Integer completedSettlementAmount,
    List<DeliveredDetailsDto> deliveredDetails
) {

}
