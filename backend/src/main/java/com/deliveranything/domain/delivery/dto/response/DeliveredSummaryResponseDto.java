package com.deliveranything.domain.delivery.dto.response;

import java.util.List;

public record DeliveredSummaryResponseDto(
    Integer thisWeekDeliveredCount,
    Integer totalDeliveryCharges,
    Integer waitingSettlementAmount,
    Integer completedSettlementAmount,
    List<DeliveredDetailsDto> deliveredDetails
) {

}
