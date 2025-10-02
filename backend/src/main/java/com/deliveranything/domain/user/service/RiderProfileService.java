package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.domain.user.repository.ProfileRepository;
import com.deliveranything.domain.user.repository.RiderProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배달원 프로필 전용 서비스 - Rider 프로필 생성/조회/수정 - 배달 상태 관리 (toggleStatus) - 배달 가능 여부 확인 - 활동 지역 관리 - 은행 계좌 정보
 * 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderProfileService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final RiderProfileRepository riderProfileRepository;

  // ================================
  // 배달원 프로필 생성/조회
  // ================================

  /**
   * 배달원 프로필 생성 (Profile 기반)
   */
  @Transactional
  public RiderProfile createProfile(Long userId, String nickname, String area,
      String licenseNumber) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 이미 배달원 프로필이 존재하는지 확인
    if (hasProfile(userId)) {
      log.warn("이미 배달원 프로필이 존재합니다: userId={}", userId);
      return getProfile(userId);
    }

    // 1단계: Profile 생성 (전역 고유 ID)
    Profile profile = Profile.builder()
        .user(user)
        .type(ProfileType.RIDER)
        .build();
    Profile savedProfile = profileRepository.save(profile);

    // 2단계: RiderProfile 생성 (Profile ID와 동일한 ID 사용)
    RiderProfile riderProfile = RiderProfile.builder()
        .profile(savedProfile)
        .nickname(nickname)
        .profileImageUrl(null)
        .toggleStatus(RiderToggleStatus.OFF) // 기본 배달 대기 상태
        .area(area)
        .licenseNumber(licenseNumber)
        .bankName("")
        .bankAccountNumber("")
        .bankAccountHolderName("")
        .build();

    RiderProfile saved = riderProfileRepository.save(riderProfile);
    log.info("배달원 프로필 생성 완료: userId={}, profileId={}", userId, saved.getId());

    return saved;
  }

  /**
   * 배달원 프로필 조회 (Profile ID)
   */
  public RiderProfile getRiderProfileById(Long riderProfileId) {
    return riderProfileRepository.findById(riderProfileId)
        .orElseThrow(() -> new CustomException(ErrorCode.RIDER_NOT_FOUND));
  }

  /**
   * 배달원 프로필 조회 (User ID)
   */
  public RiderProfile getProfile(Long userId) {
    return riderProfileRepository.findById(userId).orElse(null);
  }

  /**
   * 배달원 프로필 존재 여부 확인
   */
  public boolean hasProfile(Long userId) {
    return getProfile(userId) != null;
  }

  // 배달 상태 관리 (RiderToggleStatus)

  /**
   * 배달 상태 토글 (ON ↔ OFF)
   */
  @Transactional
  public void toggleDeliveryStatus(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    profile.toggleStatus();
    riderProfileRepository.save(profile);
    log.info("배달원 배달 상태 토글 완료 - Profile ID: {}, 현재 상태: {}",
        riderProfileId, profile.getToggleStatus());
  }

  /**
   * 배달 시작 (ON)
   */
  @Transactional
  public void startDelivery(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    profile.updateToggleStatus(RiderToggleStatus.ON);
    riderProfileRepository.save(profile);
    log.info("배달 시작 - Profile ID: {}", riderProfileId);
  }

  /**
   * 배달 종료 (OFF)
   */
  @Transactional
  public void stopDelivery(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    profile.updateToggleStatus(RiderToggleStatus.OFF);
    riderProfileRepository.save(profile);
    log.info("배달 종료 - Profile ID: {}", riderProfileId);
  }

  /**
   * 배달 가능 여부 확인 Profile.isActive && RiderToggleStatus.ON 모두 체크
   */
  public boolean isAvailableForDelivery(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    return profile.isAvailableForDelivery();
  }

  /**
   * 현재 배달 상태 조회
   */
  public RiderToggleStatus getDeliveryStatus(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    return profile.getToggleStatus();
  }

  // 배달원 프로필 정보 수정

  /**
   * 활동 지역 수정
   */
  @Transactional
  public void updateDeliveryArea(Long riderProfileId, String newArea) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    profile.updateDeliveryArea(newArea);
    riderProfileRepository.save(profile);
    log.info("활동 지역 변경 완료 - Profile ID: {}, New Area: {}", riderProfileId, newArea);
  }

  /**
   * 은행 계좌 정보 수정
   */
  @Transactional
  public void updateBankInfo(Long riderProfileId, String bankName, String accountNumber,
      String accountHolder) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    profile.updateBankInfo(bankName, accountNumber, accountHolder);
    riderProfileRepository.save(profile);
    log.info("은행 계좌 정보 수정 완료 - Profile ID: {}, Bank: {}", riderProfileId, bankName);
  }

  // ================================
  // 활동 지역 조회
  // ================================

  /**
   * 활동 지역 조
   */
  public String getDeliveryArea(Long riderProfileId) {
    RiderProfile profile = getRiderProfileById(riderProfileId);
    return profile.getArea();
  }
}