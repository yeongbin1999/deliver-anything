package com.deliveranything.domain.delivery.dto;

public record OrderDeliveryCreatedEvent(
    String orderId,
    String storeName,
    double storeLat,
    double storeLon,
    double customerLat,
    double customerLon
) {

}
