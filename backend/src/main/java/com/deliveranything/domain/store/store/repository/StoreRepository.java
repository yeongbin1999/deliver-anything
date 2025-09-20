package com.deliveranything.domain.store.store.repository;

import com.deliveranything.domain.store.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

}