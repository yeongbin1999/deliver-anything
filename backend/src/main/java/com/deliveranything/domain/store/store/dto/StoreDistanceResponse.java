package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.enums.StoreStatus;
import lombok.Getter;

@Getter
public class StoreDistanceResponse {
    private final Long id;
    private final String name;
    private final String roadAddr;
    private final StoreStatus status;
    private final boolean isOpenNow;
    private final boolean acceptingOrders;
    private final Double distance;
    private final int deliveryFee;

    public StoreDistanceResponse(Store store, Double distance, int deliveryFee) {
        this.id = store.getId();
        this.name = store.getName();
        this.roadAddr = store.getRoadAddr();
        this.status = store.getStatus();
        this.isOpenNow = store.isOpenNow();
        this.acceptingOrders = store.isAcceptingOrders();
        this.distance = distance;
        this.deliveryFee = deliveryFee;
    }
}
