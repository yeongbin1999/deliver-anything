package com.deliveranything.domain.store.store.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreSliceResponse {
    private List<StoreDistanceResponse> stores;
    private Cursor nextCursor;
    private boolean hasNext;
}
