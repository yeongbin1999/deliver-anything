package com.deliveranything.domain.store.store.dto;

public record StoreUpdateRequest(
    Long storeCategoryId,
    String name,
    String description,
    String roadAddr,
    Double lat,
    Double lng
) {

}