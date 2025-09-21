package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.enums.StoreStatus;

public record StoreSearchCondition(
    String name,
    StoreStatus status
) {

}
