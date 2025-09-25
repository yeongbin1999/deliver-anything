package com.deliveranything.domain.search.store.dto;

public record StoreSearchResponse(
    Long id,
    String name,
    String roadAddr,
    boolean isOpenNow,
    String imageUrl,
    String category,
    Double distance,
    int deliveryFee
) {

}