package com.deliveranything.domain.store.store.repository;

import com.deliveranything.domain.store.store.dto.StoreSearchCondition;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.dto.StoreSearchRequest;
import com.querydsl.core.Tuple;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.deliveranything.domain.store.store.enums.StoreCategoryType;

public interface StoreRepositoryCustom {
    Page<Store> search(StoreSearchCondition condition, Pageable pageable);

    List<Tuple> searchByDistance(Double lat, Double lng, StoreCategoryType categoryType, String keyword, int limit, Double cursorDistance, Long cursorId);
}
