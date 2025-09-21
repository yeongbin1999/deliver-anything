package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.enums.StoreCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreCreateRequest(
    @NotNull
    Long sellerProfileId,

    @NotNull
    StoreCategoryType storeCategory,

    @NotBlank
    String name,

    String roadAddr,

    @NotNull
    Double lat,

    @NotNull
    Double lng,

    String openHoursJson
) {

}
