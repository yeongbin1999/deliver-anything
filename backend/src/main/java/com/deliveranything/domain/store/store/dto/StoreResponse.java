package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.entity.Store;

public record StoreResponse(
    Long id,
    String name,
    String description,
    String roadAddr,
    boolean isOpenNow,
    String imageUrl,
    String category
) {
  public static StoreResponse from(Store store) {
    return new StoreResponse(
        store.getId(),
        store.getName(),
        store.getDescription(),
        store.getRoadAddr(),
        store.isOpenNow(),
        store.getImageUrl(),
        store.getStoreCategory().getName()
    );
  }
}
