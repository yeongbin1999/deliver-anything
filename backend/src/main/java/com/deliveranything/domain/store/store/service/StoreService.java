package com.deliveranything.domain.store.store.service;

import com.deliveranything.domain.order.dto.OrderResponse;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.store.category.entity.StoreCategory;
import com.deliveranything.domain.store.category.service.StoreCategoryService;
import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.dto.StoreOrderCursorResponse;
import com.deliveranything.domain.store.store.dto.StoreResponse;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.util.PointUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

  private final OrderService orderService;
  private final StoreRepository storeRepository;
  private final StoreCategoryService storeCategoryService;

  @Transactional
  public Long createStore(StoreCreateRequest request) {
    StoreCategory storeCategory = storeCategoryService.getById(request.storeCategoryId());

    Store store = Store.builder()
//        .sellerProfileId()
        .storeCategory(storeCategory)
        .name(request.name())
        .description(request.description())
        .roadAddr(request.roadAddr())
        .location(PointUtil.createPoint(request.lat(), request.lng()))
        .build();
    return storeRepository.save(store).getId();
  }

  @Transactional
  public Long updateStore(Long storeId, StoreUpdateRequest request) {
    Store store = storeRepository.getById(storeId);

    StoreCategory storeCategory = null;
    if (request.storeCategoryId() != null) {
      storeCategory = storeCategoryService.getById(request.storeCategoryId());
    }

    store.update(storeCategory, request.name(), request.description(), request.roadAddr(),
        request.lat() != null && request.lng() != null ? PointUtil.createPoint(request.lat(),
            request.lng()) : null);

    return store.getId();
  }

  @Transactional
  public void deleteStore(Long storeId) {
    storeRepository.deleteById(storeId);
  }

  @Transactional(readOnly = true)
  public StoreResponse getStore(Long storeId) {
    Store store = storeRepository.getById(storeId);
    return StoreResponse.from(store);
  }

  @Transactional(readOnly = true)
  public Store getById(Long storeId) {
    return storeRepository.getById(storeId);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<StoreOrderCursorResponse> getFinalizedStoreOrder(
      Long storeId,
      String nextPageToken,
      int size
  ) {
    CursorPageResponse<OrderResponse> orderPage = orderService.getStoreOrdersByCursor(storeId,
        nextPageToken, size);

    return new CursorPageResponse<>(
        orderPage.content().stream().map(StoreOrderCursorResponse::from).toList(),
        orderPage.nextPageToken(),
        orderPage.hasNext()
    );
  }
}