package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.enums.StoreStatus;

public record StoreResponse(
    Long id,
    String name,
    String roadAddr,
    StoreStatus status,
    boolean isOpenNow,
    Double distance,
    int deliveryFee
) {
    public StoreResponse(Store store, Double distance, int deliveryFee) {
        this(
            store.getId(),
            store.getName(),
            store.getRoadAddr(),
            store.getStatus(),
            store.isOpenNow(),
            distance,
            deliveryFee
        );
    }
}
