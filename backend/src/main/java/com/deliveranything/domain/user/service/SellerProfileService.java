package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.client.StoreClient;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.entity.profile.SellerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.ProfileRepository;
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
  private final ProfileRepository profileRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final StoreClient storeClient; // 외부 서비스와 통신하는 클라이언트

  // ========== 판매자 프로필 관리 ==========

  /**
   * 판매자 프로필 생성 (Profile 기반)
   */
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

    // 이미 판매자 프로필이 존재하는지 확인
    if (hasProfile(userId)) {
      log.warn("이미 판매자 프로필이 존재합니다: userId={}", userId);
      return getProfile(userId);
    }

    // 사업자 등록번호 중복 체크
    if (sellerProfileRepository.existsByBusinessCertificateNumber(businessCertificateNumber)) {
      log.warn("이미 존재하는 사업자 등록번호입니다: businessCertificateNumber={}", businessCertificateNumber);
      return null;
    }

    // 1단계: Profile 생성 (전역 고유 ID)
    Profile profile = Profile.builder()
        .user(user)
        .type(ProfileType.SELLER)
        .build();
    Profile savedProfile = profileRepository.save(profile);

    // 2단계: SellerProfile 생성 (Profile ID와 동일한 ID 사용)
    SellerProfile sellerProfile = SellerProfile.builder()
        .profile(savedProfile)
        .nickname(nickname)
        .profileImageUrl(null)
        .businessName(businessName)
        .businessCertificateNumber(businessCertificateNumber)
        .businessPhoneNumber(businessPhoneNumber)
        .bankName(bankName)
        .accountNumber(accountNumber)
        .accountHolder(accountHolder)
        .build();

    SellerProfile saved = sellerProfileRepository.save(sellerProfile);
    log.info("판매자 프로필 생성 완료: userId={}, profileId={}", userId, saved.getId());

    // 프로필 생성과 동시에 StoreClient를 통한 상점 자동 생성
    Long storeId = storeClient.createStoreForSeller(saved.getId(), businessName);
    if (storeId != null) {
      log.info("판매자용 상점 생성 완료: profileId={}, storeId={}", saved.getId(), storeId);
    } else {
      log.warn("판매자용 상점 생성 실패: profileId={}", saved.getId());
      // 상점 생성 실패해도 SellerProfile은 유지
    }

    return saved;
  }

  /**
   * 판매자 프로필 조회 (Profile ID 기반)
   */
  public SellerProfile getProfileByProfileId(Long profileId) {
    return sellerProfileRepository.findByProfileId(profileId).orElse(null);
  }

  /**
   * 판매자 프로필 조회 (사용자 ID 기반)
   */
  public SellerProfile getProfile(Long userId) {
    return sellerProfileRepository.findByUserId(userId).orElse(null);
  }

  /**
   * 판매자 프로필 존재 여부 확인
   */
  public boolean hasProfile(Long userId) {
    return getProfile(userId) != null;
  }

  /**
   * Profile ID로 판매자 프로필 존재 여부 확인
   */
  public boolean existsByProfileId(Long profileId) {
    return getProfileByProfileId(profileId) != null;
  }

  /**
   * 사업자 정보 수정
   */
  @Transactional
  public boolean updateBusinessInfo(Long userId, String businessName, String businessPhoneNumber) {
    SellerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("판매자 프로필을 찾을 수 없습니다: userId={}", userId);
      return false;
    }

    profile.updateBusinessInfo(businessName, businessPhoneNumber);
    sellerProfileRepository.save(profile);

    log.info("사업자 정보 수정 완료: userId={}, profileId={}, businessName={}",
        userId, profile.getId(), businessName);
    return true;
  }

  /**
   * Profile ID로 사업자 정보 수정
   */
  @Transactional
  public boolean updateBusinessInfoByProfileId(Long profileId, String businessName,
      String businessPhoneNumber) {
    SellerProfile profile = getProfileByProfileId(profileId);
    if (profile == null) {
      log.warn("판매자 프로필을 찾을 수 없습니다: profileId={}", profileId);
      return false;
    }

    profile.updateBusinessInfo(businessName, businessPhoneNumber);
    sellerProfileRepository.save(profile);

    log.info("사업자 정보 수정 완료: profileId={}, businessName={}", profileId, businessName);
    return true;
  }

  /**
   * 정산 정보 수정
   */
  @Transactional
  public boolean updateBankInfo(Long userId, String bankName, String accountNumber,
      String accountHolder) {
    SellerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("판매자 프로필을 찾을 수 없습니다: userId={}", userId);
      return false;
    }

    profile.updateBankInfo(bankName, accountNumber, accountHolder);
    sellerProfileRepository.save(profile);

    log.info("정산 정보 수정 완료: userId={}, profileId={}, bankName={}",
        userId, profile.getId(), bankName);
    return true;
  }

  /**
   * Profile ID로 정산 정보 수정
   */
  @Transactional
  public boolean updateBankInfoByProfileId(Long profileId, String bankName, String accountNumber,
      String accountHolder) {
    SellerProfile profile = getProfileByProfileId(profileId);
    if (profile == null) {
      log.warn("판매자 프로필을 찾을 수 없습니다: profileId={}", profileId);
      return false;
    }

    profile.updateBankInfo(bankName, accountNumber, accountHolder);
    sellerProfileRepository.save(profile);

    log.info("정산 정보 수정 완료: profileId={}, bankName={}", profileId, bankName);
    return true;
  }

  /**
   * 사업자 등록번호 중복 체크
   */
  public boolean existsByBusinessCertificateNumber(String businessCertificateNumber) {
    return sellerProfileRepository.existsByBusinessCertificateNumber(businessCertificateNumber);
  }

  /**
   * 사업자 등록번호로 판매자 프로필 조회
   */
  public SellerProfile getProfileByBusinessCertificateNumber(String businessCertificateNumber) {
    return sellerProfileRepository.findByBusinessCertificateNumber(businessCertificateNumber)
        .orElse(null);
  }
}