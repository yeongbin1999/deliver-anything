package com.deliveranything.domain.store.store.dto;

import java.util.List;

public record StoreSliceResponse(
    List<StoreResponse> stores,
    String nextPageToken,
    boolean hasNext
) {

}
