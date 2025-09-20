package com.deliveranything.domain.store.store.dto;

import com.deliveranything.domain.store.store.enums.StoreStatus;
import lombok.Data;

@Data
public class StoreSearchCondition {
    private String name;
    private StoreStatus status;
}
