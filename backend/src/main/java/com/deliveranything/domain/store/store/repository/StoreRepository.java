package com.deliveranything.domain.store.store.repository;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

  default Store getById(Long storeId) {
    return findById(storeId)
        .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
  }
}