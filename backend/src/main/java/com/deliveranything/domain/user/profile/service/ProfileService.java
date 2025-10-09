package com.deliveranything.domain.user.profile.service;

import com.deliveranything.domain.user.profile.dto.onboard.CustomerOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.RiderOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.SellerOnboardingData;
import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.entity.SellerProfile;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.enums.RiderToggleStatus;
import com.deliveranything.domain.user.profile.repository.CustomerProfileRepository;
import com.deliveranything.domain.user.profile.repository.ProfileRepository;
import com.deliveranything.domain.user.profile.repository.RiderProfileRepository;
import com.deliveranything.domain.user.profile.repository.SellerProfileRepository;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 프로필 관리 서비스 BaseProfile을 활용한 공통 프로필 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final RiderProfileRepository riderProfileRepository;

  // ========== 온보딩 및 프로필 관리 ==========

  /**
   * 온보딩 완료 처리 (Profile 기반) - 타입 안전하게 개선
   */
  @Transactional
  public boolean completeOnboarding(Long userId, ProfileType selectedProfile,
      Object profileData) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 이미 온보딩 완료된 경우 방어
    if (user.isOnboardingCompleted()) {
      log.warn("이미 온보딩이 완료되었습니다: userId={}", userId);
      return false;
    }

    // 이미 해당 타입의 프로필이 존재하는지 확인
    Profile existingProfile = profileRepository.findByUserIdAndType(userId, selectedProfile)
        .orElse(null);

    if (existingProfile != null) {
      log.warn("이미 해당 프로필이 존재합니다: userId={}, profileType={}", userId, selectedProfile);
      // 이미 있으면 그냥 온보딩 완료 처리
      user.completeOnboarding(existingProfile);
      userRepository.save(user);
      return true;
    }

    // 1단계: Profile 먼저 생성
    Profile profile = createProfile(user, selectedProfile);

    // 2단계: 세부 프로필 생성 (타입별 DTO로 처리)
    boolean profileCreated = createDetailedProfile(profile, selectedProfile, profileData);
    if (!profileCreated) {
      log.warn("세부 프로필 생성 실패: userId={}, selectedProfile={}", userId, selectedProfile);
      return false;
    }

    // 3단계: 온보딩 완료 처리
    user.completeOnboarding(profile);
    userRepository.save(user);

    log.info("온보딩 완료: userId={}, selectedProfile={}, profileId={}",
        userId, selectedProfile, profile.getId());
    return true;
  }

  /**
   * 프로필 전환 - 구체적 에러 처리 추가
   */
  @Transactional
  public boolean switchProfile(Long userId, ProfileType targetProfile) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    // 온보딩 미완료 체크
    if (!user.isOnboardingCompleted()) {
      log.warn("온보딩이 완료되지 않은 사용자입니다: userId={}", userId);
      throw new CustomException(ErrorCode.ONBOARDING_NOT_COMPLETED);
    }

    // 타겟 프로필 조회
    Profile targetProfileEntity = profileRepository
        .findByUserIdAndType(userId, targetProfile)
        .orElseThrow(() -> {
          log.warn("해당 프로필을 찾을 수 없습니다: userId={}, targetProfile={}",
              userId, targetProfile);
          return new CustomException(ErrorCode.PROFILE_NOT_FOUND);
        });

    if (user.getCurrentActiveProfileType() == targetProfile) {
      log.info("이미 활성화된 프로필입니다: userId={}, targetProfile={}", userId, targetProfile);
      throw new CustomException(ErrorCode.PROFILE_ALREADY_ACTIVE);
    }

    // 프로필 활성화 상태 체크
    if (!targetProfileEntity.isActive()) {
      log.warn("비활성화된 프로필로는 전환할 수 없습니다: userId={}, targetProfile={}",
          userId, targetProfile);
      throw new CustomException(ErrorCode.PROFILE_INACTIVE);
    }

    try {
      user.switchProfile(targetProfileEntity);
      userRepository.save(user);
      log.info("프로필 전환 완료: userId={}, newActiveProfile={}, profileId={}",
          userId, targetProfile, targetProfileEntity.getId());
      return true;
    } catch (IllegalStateException e) {
      log.warn("프로필 전환 실패: userId={}, targetProfile={}, error={}",
          userId, targetProfile, e.getMessage());
      throw new CustomException(ErrorCode.PROFILE_NOT_OWNED);
    }
  }

  /**
   * 사용 가능한 프로필 목록 조회
   */
  public List<ProfileType> getAvailableProfiles(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    return user.getActiveProfileTypes();
  }

  // ========== Profile 관리 헬퍼 메서드 ==========

  /**
   * Profile 엔티티 생성 (전역 고유 ID 할당)
   */
  @Transactional
  public Profile createProfile(User user, ProfileType profileType) {
    Profile profile = Profile.builder()
        .user(user)
        .type(profileType)
        .build();

    Profile savedProfile = profileRepository.save(profile);
    log.info("Profile 생성 완료: userId={}, profileType={}, profileId={}",
        user.getId(), profileType, savedProfile.getId());

    return savedProfile;
  }

  /**
   * 프로필 타입별 세부 프로필 생성 (타입 안전하게 개선)
   */
  private boolean createDetailedProfile(Profile profile, ProfileType profileType,
      Object profileData) {
    try {
      switch (profileType) {
        case CUSTOMER -> {
          if (!(profileData instanceof CustomerOnboardingData data)) {
            log.error("잘못된 고객 프로필 데이터 타입: {}", profileData.getClass());
            return false;
          }

          CustomerProfile customerProfile = CustomerProfile.builder()
              .profile(profile)
              .nickname(data.nickname())
              .profileImageUrl(data.profileImageUrl())
              .build();
          customerProfileRepository.save(customerProfile);
          log.info("고객 프로필 생성 완료: profileId={}", profile.getId());
        }
        case SELLER -> {
          if (!(profileData instanceof SellerOnboardingData data)) {
            log.error("잘못된 판매자 프로필 데이터 타입: {}", profileData.getClass());
            return false;
          }

          SellerProfile sellerProfile = SellerProfile.builder()
              .profile(profile)
              .nickname(data.nickname())
              .profileImageUrl(data.profileImageUrl())
              .businessName(data.businessName())
              .businessCertificateNumber(data.businessCertificateNumber())
              .businessPhoneNumber(data.businessPhoneNumber())
              .bankName(data.bankName())
              .accountNumber(data.accountNumber())
              .accountHolder(data.accountHolder())
              .build();
          sellerProfileRepository.save(sellerProfile);
          log.info("판매자 프로필 생성 완료: profileId={}", profile.getId());
        }
        case RIDER -> {
          if (!(profileData instanceof RiderOnboardingData data)) {
            log.error("잘못된 배달원 프로필 데이터 타입: {}", profileData.getClass());
            return false;
          }

          String area = data.area() != null && !data.area().isBlank() ? data.area() : "서울";

          RiderProfile riderProfile = RiderProfile.builder()
              .profile(profile)
              .nickname(data.nickname())
              .profileImageUrl(data.profileImageUrl())
              .toggleStatus(RiderToggleStatus.OFF)
              .area(area)
              .licenseNumber(data.licenseNumber())
              .bankName("")
              .bankAccountNumber("")
              .bankAccountHolderName("")
              .build();
          riderProfileRepository.save(riderProfile);
          log.info("배달원 프로필 생성 완료: profileId={}", profile.getId());
        }
        default -> {
          log.error("지원하지 않는 프로필 타입: {}", profileType);
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      log.error("세부 프로필 생성 중 오류 발생: profileType={}, error={}", profileType, e.getMessage(), e);
      return false;
    }
  }

  /**
   * 특정 사용자의 특정 프로필 타입 조회
   */
  public Profile getProfileByUserAndType(Long userId, ProfileType profileType) {
    return profileRepository.findByUserIdAndType(userId, profileType).orElse(null);
  }

  /**
   * 사용자의 모든 활성 프로필 조회
   */
  public List<Profile> getActiveProfilesByUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    return profileRepository.findActiveProfilesByUser(user);
  }
}