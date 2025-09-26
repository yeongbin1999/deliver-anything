package com.deliveranything.domain.store.store.service;

import com.deliveranything.domain.order.entity.Order;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.store.category.entity.StoreCategory;
import com.deliveranything.domain.store.category.service.StoreCategoryService;
import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.dto.StoreFinalizedOrderResponse;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
import com.deliveranything.global.util.PointUtil;
import java.time.LocalDateTime;
import java.util.List;
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
    StoreCategory storeCategory = storeCategoryService.findById(request.storeCategoryId());

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
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

    StoreCategory storeCategory = null;
    if (request.storeCategoryId() != null) {
      storeCategory = storeCategoryService.findById(request.storeCategoryId());
    }

    store.update(storeCategory, request.name(), request.description(), request.roadAddr(),
        request.lat() != null && request.lng() != null ? PointUtil.createPoint(request.lat(), request.lng()) : null);

    return store.getId();
  }

  @Transactional
  public void deleteStore(Long storeId) {
    storeRepository.deleteById(storeId);
  }

  @Transactional(readOnly = true)
  public Store findById(Long storeId) {
    return storeRepository.findById(storeId)
        .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
  }

  @Transactional(readOnly = true) // TODO: 인자는 입맛대로 바꾸셔요 이해를 위해 분해해 놨을 뿐입니다
  public CursorPageResponse<StoreFinalizedOrderResponse> getFinalizedStoreOrder(
      Long storeId,
      String nextPageToken,
      int size
  ) {
    LocalDateTime lastCreatedAt = null;
    Long lastOrderId = null;
    String[] decodedParts = CursorUtil.decode(nextPageToken);

    if (decodedParts != null && decodedParts.length == 2) {
      try {
        lastCreatedAt = LocalDateTime.parse(decodedParts[0]);
        lastOrderId = Long.parseLong(decodedParts[1]);
      } catch (NumberFormatException e) {
        lastCreatedAt = null;
        lastOrderId = null;
      }
    }

    List<Order> finalizedOrders = orderService.getStoreOrdersByCursor(storeId,
        List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED), lastCreatedAt, lastOrderId, size + 1);

    List<StoreFinalizedOrderResponse> finalizedOrderResponses = finalizedOrders.stream()
        .limit(size)
        .map(StoreFinalizedOrderResponse::from)
        .toList();

    boolean hasNext = finalizedOrders.size() > size;
    StoreFinalizedOrderResponse lastResponse = finalizedOrderResponses.getLast();

    return new CursorPageResponse<>(
        finalizedOrderResponses,
        hasNext ? CursorUtil.encode(lastResponse.createdAt(), lastResponse.orderId()) : null,
        hasNext
    );
  }
}