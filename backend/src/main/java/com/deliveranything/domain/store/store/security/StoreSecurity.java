package com.deliveranything.domain.store.store.security;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreSecurity {

  private final StoreService storeService;

  public boolean isOwner(Long storeId, SecurityUser user) {
    Store store = storeService.getById(storeId);
    return store.getSellerProfileId().equals(user.getCurrentActiveProfile().getId());
  }
}