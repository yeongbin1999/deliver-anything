package com.deliveranything.domain.store.category.repository;

import com.deliveranything.domain.store.category.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {

}

