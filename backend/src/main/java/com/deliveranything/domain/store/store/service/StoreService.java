package com.deliveranything.domain.store.store.service;

import com.deliveranything.domain.store.store.dto.Cursor;
import com.deliveranything.domain.store.store.dto.StoreDistanceResponse;
import com.deliveranything.domain.store.store.dto.StoreSearchRequest;
import com.deliveranything.domain.store.store.dto.StoreSliceResponse;
import com.deliveranything.domain.store.store.dto.StoreSearchCondition;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import com.querydsl.core.Tuple;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

  private final StoreRepository storeRepository;

  // This is the old offset-based search, can be removed if not needed.
  public Page<Store> searchStores(StoreSearchCondition condition, Pageable pageable) {
    return storeRepository.search(condition, pageable);
  }

  public StoreSliceResponse searchByDistance(StoreSearchRequest request) {
    // To check for hasNext, fetch one more item than the requested limit
    int queryLimit = request.getLimit() + 1;
    request.setLimit(queryLimit);

    List<Tuple> results = storeRepository.searchByDistance(request);

    List<StoreDistanceResponse> stores = results.stream()
        .map(this::mapToStoreDistanceResponse)
        .collect(Collectors.toList());

    boolean hasNext = stores.size() == queryLimit;
    if (hasNext) {
      // Remove the extra item used for the hasNext check
      stores.remove(stores.size() - 1);
    }

    Cursor nextCursor = null;
    if (!stores.isEmpty()) {
      StoreDistanceResponse lastStore = stores.get(stores.size() - 1);
      // The cursor is based on the last item of the *actual* page
      nextCursor = new Cursor(lastStore.getDistance(), lastStore.getId());
    }

    return new StoreSliceResponse(stores, nextCursor, hasNext);
  }

  private StoreDistanceResponse mapToStoreDistanceResponse(Tuple tuple) {
    Store store = tuple.get(0, Store.class);
    Double distance = tuple.get(1, Double.class);
    int deliveryFee = calculateDeliveryFee(distance);
    return new StoreDistanceResponse(store, distance, deliveryFee);
  }

  private int calculateDeliveryFee(Double distanceInMeters) {
    if (distanceInMeters == null) {
      return 99999; // Or some default error value
    }
    if (distanceInMeters <= 2000) { // ~2km
      return 3000;
    }
    if (distanceInMeters <= 5000) { // ~5km
      return 5000;
    }
    // up to 10km (max search radius)
    return 7000;
  }
}
