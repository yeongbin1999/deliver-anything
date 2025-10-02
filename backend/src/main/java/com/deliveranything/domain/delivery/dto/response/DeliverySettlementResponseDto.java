package com.deliveranything.domain.delivery.dto.response;

import com.deliveranything.global.common.CursorPageResponse;
import lombok.Builder;

@Builder
public record DeliverySettlementResponseDto(
    Integer totalDeliveredCount,
    Long thisWeekTotalEarnings,
    Long thisMonthTotalEarnings,
    Long pendingSettlementAmount,
    Long totalEarnings,
    CursorPageResponse<DeliveredSettlementDetailsDto> deliveredSettlementDetails
) {

}
