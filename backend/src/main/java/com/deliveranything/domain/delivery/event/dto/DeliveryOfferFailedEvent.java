package com.deliveranything.domain.delivery.event.dto;

import com.deliveranything.domain.order.event.OrderStoreAcceptedEvent;

public record DeliveryOfferFailedEvent(OrderStoreAcceptedEvent order) {

}
