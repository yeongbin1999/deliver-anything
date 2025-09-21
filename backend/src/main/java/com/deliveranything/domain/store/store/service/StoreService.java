package com.deliveranything.domain.store.store.service;

import com.deliveranything.global.common.SliceResponse;
import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.dto.StoreResponse;
import com.deliveranything.domain.store.store.dto.StoreSearchRequest;
import com.deliveranything.domain.store.store.dto.StoreUpdateRequest;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.util.CursorUtil;
import com.deliveranything.global.util.PointUtil;
import com.querydsl.core.Tuple;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public Long createStore(StoreCreateRequest request) {
        Store store = Store.builder()
//            .sellerProfileId() 추후 인증 인가 추가시 반영
            .storeCategory(request.storeCategory())
            .name(request.name())
            .roadAddr(request.roadAddr())
            .location(PointUtil.createPoint(request.lat(), request.lng()))
            .openHoursJson(request.openHoursJson())
            .build();
        return storeRepository.save(store).getId();
    }

    @Transactional
    public Long updateStore(Long storeId, StoreUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        store.update(request);
        return store.getId();
    }

    @Transactional
    public void deleteStore(Long storeId) {
        storeRepository.deleteById(storeId);
    }

    public SliceResponse<StoreResponse> search(StoreSearchRequest request) {
        // 1. Decode cursor using generic utility
        Double cursor = null;
        Long cursorId = null;
        String[] decodedParts = CursorUtil.decode(request.nextPageToken());

        if (decodedParts != null && decodedParts.length == 2) {
            try {
                cursor = Double.parseDouble(decodedParts[0]);
                cursorId = Long.parseLong(decodedParts[1]);
            } catch (NumberFormatException e) {
                // Invalid cursor format, treat as first page
                cursor = null;
                cursorId = null;
            }
        }

        // 2. Convert categoryId to Enum
        com.deliveranything.domain.store.store.enums.StoreCategoryType categoryType = null;
        if (request.categoryId() != null) {
            categoryType = com.deliveranything.domain.store.store.enums.StoreCategoryType.fromId(request.categoryId());
        }

        // 3. Call repository with decoded values (+1 limit for hasNext check)
        int limit = (request.limit() == null || request.limit() == 0) ? 10 : request.limit();
        int queryLimit = limit + 1;
        List<Tuple> results = storeRepository.searchByDistance(
            request.lat(), request.lng(), categoryType,
            request.keyword(), queryLimit, cursor, cursorId
        );

        // 4. Map to DTO and calculate delivery fee
        List<StoreResponse> stores = results.stream()
            .map(this::mapToStoreDistanceResponse)
            .collect(Collectors.toList());

        // 5. Slice and create next cursor
        boolean hasNext = stores.size() == queryLimit;
        if (hasNext) {
            stores.remove(stores.size() - 1);
        }

        String nextPageToken = null;
        if (!stores.isEmpty()) {
            StoreResponse lastStore = stores.get(stores.size() - 1);
            nextPageToken = CursorUtil.encode(lastStore.distance(), lastStore.id());
        }

        return new SliceResponse<>(stores, nextPageToken, hasNext);
    }

    private StoreResponse mapToStoreDistanceResponse(Tuple tuple) {
        Store store = tuple.get(0, Store.class);
        Double distance = tuple.get(1, Double.class);
        int deliveryFee = calculateDeliveryFee(distance);
        return new StoreResponse(store, distance, deliveryFee);
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
