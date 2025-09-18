package com.deliveranything.domain.delivery.dto.request;


import jakarta.validation.constraints.NotNull;

public record DeliveryAreaRequestDto(
    @NotNull
    Long riderProfileId,

    @NotNull
    String deliveryArea
) {

}
