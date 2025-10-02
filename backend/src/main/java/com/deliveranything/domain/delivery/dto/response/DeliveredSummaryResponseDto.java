package com.deliveranything.domain.delivery.dto.response;

import com.deliveranything.global.common.CursorPageResponse;
import lombok.Builder;

@Builder
public record DeliveredSummaryResponseDto(
    Integer thisWeekDeliveredCount,
    Long waitingSettlementAmount,
    Long completedSettlementAmount,
    CursorPageResponse<DeliveredDetailsDto> deliveredDetails
) {

}
