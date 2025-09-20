package com.deliveranything.domain.store.store.dto;

import lombok.Data;

@Data
public class StoreSearchRequest {
    private Double lat;
    private Double lng;
    private Long categoryId;
    private String name;
    private int limit = 10;
    private Double cursorDistance;
    private Long cursorId;
}