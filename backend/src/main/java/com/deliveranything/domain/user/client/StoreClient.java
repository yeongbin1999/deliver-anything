package com.deliveranything.domain.user.client;

import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
import com.deliveranything.domain.store.store.enums.StoreCategoryType;
import com.deliveranything.domain.store.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/***
 * 웹서퍼 멘토님 조언대로 Client 구현 방식을 통해 다른 도메인 서비스코드와 연동해봤습니다.
 ***/
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreClient {

  private final StoreService storeService;

  // 판매자용 기본 상점 생성
  public Long createStoreForSeller(Long sellerProfileId, String businessName) {
    try {
      // Record 생성자 직접 사용
      StoreCreateRequest request = new StoreCreateRequest(
          StoreCategoryType.FOOD_CAFE,    // 음식/카페로 변경
          businessName,
          "미설정",
          37.5665,
          126.9780,
          "{}"
      );

      /*** 기존 StoreService 메서드 호출
       * StoreService의 createStore에 sellerProfileId 파라미터만 추가하면 바로 연동!
       ***/
      // Long storeId = storeService.createStore(sellerProfileId, request);
      Long storeId = storeService.createStore(request); //
      log.info("판매자용 상점 생성 성공: sellerProfileId={}, storeId={}", sellerProfileId, storeId);
      return storeId;

    } catch (Exception e) {
      log.error("판매자용 상점 생성 실패: sellerProfileId={}", sellerProfileId, e);
      return null; // 실패해도 SellerProfile 생성은 유지
    }
  }

  // 판매자가 상점을 소유하고 있는지 확인 - 필요해 질 시 hasStore 요청
//  public boolean hasStore(Long sellerProfileId) {
//    try {
//      return storeService.hasStore(sellerProfileId);
//    } catch (Exception e) {
//      log.error("상점 존재 여부 확인 실패: sellerProfileId={}", sellerProfileId, e);
//      return false;
//    }
//  }
}