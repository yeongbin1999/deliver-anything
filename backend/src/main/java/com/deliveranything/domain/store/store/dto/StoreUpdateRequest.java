package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.enums.StoreCategoryType;

public record StoreUpdateRequest(
    StoreCategoryType storeCategory,
    String name,
    String roadAddr,
    Double lat,
    Double lng,
    String openHoursJson
) {

}
