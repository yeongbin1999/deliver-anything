package com.deliveranything.domain.delivery.dto.request;


import jakarta.validation.constraints.NotNull;

public record DeliveryAreaRequestDto(
    // 추후 JWT에서 사용자 정보-Profile ID가 넘어오면 변경 예정
    @NotNull
    Long riderProfileId,

    @NotNull
    String deliveryArea
) {

}
