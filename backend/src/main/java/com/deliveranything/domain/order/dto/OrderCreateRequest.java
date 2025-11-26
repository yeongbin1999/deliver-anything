package com.deliveranything.domain.order.dto;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.global.util.PointUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderCreateRequest(
    @NotNull @Positive Long storeId,
    @NotNull @NotEmpty @Valid List<OrderItemRequest> orderItemRequests,
    @NotBlank @Size(max = 100) String address,
    @NotBlank @Size(max = 120) String storeName,
    @NotNull Double lat,
    @NotNull Double lng,
    @Size(max = 30) String riderNote,
    @Size(max = 30) String storeNote,
    @NotNull @Positive Long totalPrice,
    @NotNull @Positive Long storePrice,
    @NotNull @Positive Long deliveryPrice
) {

  public Order toEntity(CustomerProfile customerProfile, Store store) {
    return Order.builder()
        .customerProfile(customerProfile)
        .store(store)
        .address(this.address)
        .storeName(this.storeName)
        .destination(PointUtil.createPoint(this.lat, this.lng))
        .riderNote(this.riderNote)
        .storeNote(this.storeNote)
        .totalPrice(this.totalPrice)
        .storePrice(this.storePrice)
        .deliveryPrice(this.deliveryPrice)
        .build();
  }
}
