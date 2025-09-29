package com.deliveranything.domain.product.product.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductKeywordsChangedEvent {
    private final Long storeId;
    private final Long productId;
}
