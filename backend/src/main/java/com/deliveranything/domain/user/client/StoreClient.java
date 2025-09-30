//package com.deliveranything.domain.user.client;
//
//import com.deliveranything.domain.store.store.dto.StoreCreateRequest;
//import com.deliveranything.domain.store.store.service.StoreService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * 수정된 StoreClient - 전역 고유 Profile ID 사용 다른 도메인 서비스와의 연동을 담당
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StoreClient {
//
//  private final StoreService storeService;
//
//  /**
//   * 판매자용 기본 상점 생성 (Profile ID 기반)
//   *
//   * @param sellerProfileId 전역 고유 Profile ID (더이상 SellerProfile의 개별 ID가 아님)
//   * @param businessName    상점 이름
//   * @return Store ID (상점 생성 성공시) 또는 null (실패시)
//   */
//  public Long createStoreForSeller(Long sellerProfileId, String businessName) {
//    log.info("판매자용 상점 생성 시작: profileId={}, businessName={}", sellerProfileId, businessName);
//
//    try {
//      // StoreCreateRequest 생성 (Record 생성자 직접 사용)
//      StoreCreateRequest request = new StoreCreateRequest(
//          1L,                          // 기본 카테고리 ID (예시: 1L, 실제 값은 필요에 따라 수정)
//          businessName,                // 사업체명을 상점명으로 사용
//          "미설정",                    // 설명 - 나중에 판매자가 수정 가능
//          "미설정",                    // 주소 - 나중에 판매자가 수정 가능
//          37.5665,                     // 기본 위치 - 서울 (위도)
//          126.9780                     // 기본 위치 - 서울 (경도)
//      );
//
//      // StoreService 호출 - sellerProfileId는 전역 고유 Profile ID
//      // 만약 StoreService가 sellerProfileId 파라미터를 받도록 수정되었다면:
//      // Long storeId = storeService.createStore(sellerProfileId, request);
//
//      // 현재 StoreService가 sellerProfileId를 받지 않는다면 임시 방편:
//      Long storeId = storeService.createStore(request);
//
//      // TODO: Store 엔티티에 sellerProfileId 필드 추가 필요
//      // 또는 Store 생성 후 별도로 매핑 테이블에 관계 저장
//
//      if (storeId != null) {
//        log.info("판매자용 상점 생성 성공: profileId={}, storeId={}, businessName={}",
//            sellerProfileId, storeId, businessName);
//        return storeId;
//      } else {
//        log.warn("판매자용 상점 생성 실패: profileId={}, businessName={}",
//            sellerProfileId, businessName);
//        return null;
//      }
//
//    } catch (Exception e) {
//      log.error("판매자용 상점 생성 중 오류 발생: profileId={}, businessName={}",
//          sellerProfileId, businessName, e);
//      return null; // 상점 생성 실패해도 SellerProfile 생성은 유지
//    }
//  }
//
//  /**
//   * 판매자가 상점을 소유하고 있는지 확인
//   *
//   * @param sellerProfileId 전역 고유 Profile ID
//   * @return 상점 소유 여부
//   */
//  public boolean hasStore(Long sellerProfileId) {
//    try {
//      // TODO: StoreService에 hasStoreBySellerProfileId 메서드 추가 필요
//      // 현재는 StoreService에 해당 메서드가 없으므로 임시 구현
//
//      log.debug("상점 존재 여부 확인: profileId={}", sellerProfileId);
//
//      // Store 팀에서 구현해야 할 메서드:
//      // return storeService.existsBySellerProfileId(sellerProfileId);
//
//      // 임시 방편 - 항상 true 반환 (실제로는 Store 서비스에서 구현 필요)
//      return true;
//
//    } catch (Exception e) {
//      log.error("상점 존재 여부 확인 실패: profileId={}", sellerProfileId, e);
//      return false;
//    }
//  }
//
//  /**
//   * 판매자의 상점 ID 조회
//   *
//   * @param sellerProfileId 전역 고유 Profile ID
//   * @return Store ID 또는 null
//   */
//  public Long getStoreIdBySellerProfile(Long sellerProfileId) {
//    try {
//      // TODO: StoreService에 findStoreIdBySellerProfileId 메서드 추가 필요
//      log.debug("판매자 상점 ID 조회: profileId={}", sellerProfileId);
//
//      // Store 팀에서 구현해야 할 메서드:
//      // return storeService.findStoreIdBySellerProfileId(sellerProfileId);
//
//      // 임시 방편 - null 반환
//      return null;
//
//    } catch (Exception e) {
//      log.error("판매자 상점 ID 조회 실패: profileId={}", sellerProfileId, e);
//      return null;
//    }
//  }
//
//  /**
//   * 판매자의 상점 정보 조회
//   *
//   * @param sellerProfileId 전역 고유 Profile ID
//   * @return Store 정보 또는 null
//   */
//  public Object getStoreBySellerProfile(Long sellerProfileId) {
//    try {
//      // TODO: StoreService에 findBySellerProfileId 메서드 추가 필요
//      log.debug("판매자 상점 정보 조회: profileId={}", sellerProfileId);
//
//      // Store 팀에서 구현해야 할 메서드:
//      // return storeService.findBySellerProfileId(sellerProfileId);
//
//      // 임시 방편 - null 반환
//      return null;
//
//    } catch (Exception e) {
//      log.error("판매자 상점 정보 조회 실패: profileId={}", sellerProfileId, e);
//      return null;
//    }
//  }
//
//  /**
//   * 판매자 상점 정보 업데이트
//   *
//   * @param sellerProfileId 전역 고유 Profile ID
//   * @param businessName    새로운 상점명
//   * @return 업데이트 성공 여부
//   */
//  public boolean updateStoreBusinessName(Long sellerProfileId, String businessName) {
//    try {
//      log.info("판매자 상점 정보 업데이트: profileId={}, businessName={}",
//          sellerProfileId, businessName);
//
//      // TODO: StoreService에 updateBusinessNameBySellerProfileId 메서드 추가 필요
//      // Store 팀에서 구현해야 할 메서드:
//      // return storeService.updateBusinessNameBySellerProfileId(sellerProfileId, businessName);
//
//      // 임시 방편 - 성공으로 가정
//      return true;
//
//    } catch (Exception e) {
//      log.error("판매자 상점 정보 업데이트 실패: profileId={}, businessName={}",
//          sellerProfileId, businessName, e);
//      return false;
//    }
//  }
//}
//
//// ================================
//// 3. Store 팀에서 구현해야 할 Repository 메서드들 (요청사항)
//// ================================
//
///*
// * Store 팀에 요청할 Repository 메서드들:
// *
// * public interface StoreRepository extends JpaRepository<Store, Long> {
// *
// *   // 판매자 Profile ID로 상점 존재 여부 확인
// *   boolean existsBySellerProfileId(Long sellerProfileId);
// *
// *   // 판매자 Profile ID로 상점 조회
// *   Optional<Store> findBySellerProfileId(Long sellerProfileId);
// *
// *   // 판매자 Profile ID로 상점 ID 조회
// *   @Query("SELECT s.id FROM Store s WHERE s.sellerProfileId = :sellerProfileId")
// *   Optional<Long> findStoreIdBySellerProfileId(@Param("sellerProfileId") Long sellerProfileId);
// *
// *   // 판매자 Profile ID로 상점 목록 조회 (다중 상점 허용시)
// *   List<Store> findAllBySellerProfileId(Long sellerProfileId);
// * }
// *
// * Store 엔티티에 sellerProfileId 필드 추가 필요:
// *
// * @Entity
// * @Table(name = "stores")
// * public class Store {
// *   // ... 기존 필드들
// *
// *   @Column(name = "seller_profile_id", nullable = false)
// *   private Long sellerProfileId; // User 도메인의 전역 고유 Profile ID
// *
// *   // ... 나머지 구현
// * }
// */