package com.deliveranything.domain.user.service;


import com.deliveranything.domain.user.client.StoreClient;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.SellerProfile;
import com.deliveranything.domain.user.repository.SellerProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerProfileService {

  private final UserRepository userRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final StoreClient storeClient; // 외부 서비스와 통신하는 클라이언트

  // 판매자 프로필 관리

  // 판매자 프로필 생성 (UserService에서 온보딩 시 호출 예정)
  @Transactional
  public SellerProfile createProfile(Long userId, String nickname, String businessName,
      String businessCertificateNumber, String businessPhoneNumber,
      String bankName, String accountNumber, String accountHolder) {

    // 사용자 존재 여부 확인
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    // 이미 판매자 프로필이 존재하는지 여부 확인
    if (user.getSellerProfile() != null) {
      log.warn("이미 판매자 프로필이 존재합니다: userId={}", userId);
      return user.getSellerProfile();
    }

    // 사업자 등록번호 중복 체크
    if (sellerProfileRepository.existsByBusinessCertificateNumber(businessCertificateNumber)) {
      log.warn("이미 존재하는 사업자 등록번호입니다: businessCertificateNumber={}", businessCertificateNumber);
      return null;
    }

    SellerProfile sellerProfile = SellerProfile.builder()
        .user(user)
        .nickname(nickname)
        .profileImageUrl(null)  // 기본값
        .businessName(businessName)
        .businessCertificateNumber(businessCertificateNumber)
        .businessPhoneNumber(businessPhoneNumber)
        .bankName(bankName)
        .accountNumber(accountNumber)
        .accountHolder(accountHolder)
        .build();

    sellerProfileRepository.save(sellerProfile);
    log.info("판매자 프로필 생성 완료: userId={}, profileId={}", userId, sellerProfile.getId());

    // 프로필 생성과 동시에 StoreClient를 통한 상점 자동 생성
    Long storeId = storeClient.createStoreForSeller(sellerProfile.getId(), businessName);
    if (storeId != null) {
      log.info("판매자용 상점 생성 완료: sellerId={}, storeId={}", sellerProfile.getId(), storeId);
    } else {
      log.warn("판매자용 상점 생성 실패: sellerId={}", sellerProfile.getId());
      // 상점 생성 실패해도 SellerProfile은 유지
    }

    return sellerProfile;
  }
}
