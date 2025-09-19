package com.deliveranything.domain.store.repository;

import com.deliveranything.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {


}