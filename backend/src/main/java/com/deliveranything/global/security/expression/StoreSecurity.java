package com.deliveranything.global.security.expression;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.service.StoreService;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.auth.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreSecurity {

  private final StoreService storeService;

  public boolean isOwner(Long storeId, SecurityUser user) {
    Store store = storeService.getStoreById(storeId);

    if (!store.getSellerProfileId().equals(user.getCurrentActiveProfile().getId())) {
      throw new CustomException(ErrorCode.STORE_OWNER_MISMATCH);
    }

    return true;
  }
}