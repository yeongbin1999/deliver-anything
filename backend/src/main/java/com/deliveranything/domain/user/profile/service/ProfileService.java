package com.deliveranything.domain.user.profile.service;

import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.entity.SellerProfile;
<<<<<<< HEAD
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
import java.util.Map;
=======
import com.deliveranything.domain.user.profile.rerpository.CustomerProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.ProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.RiderProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.SellerProfileRepository;
>>>>>>> 2511efe (refactor(be) : 기존 user 패키지를 auth 와 user/user , user/profile 로 분리)
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

<<<<<<< HEAD
  private final UserRepository userRepository;
=======
>>>>>>> 2511efe (refactor(be) : 기존 user 패키지를 auth 와 user/user , user/profile 로 분리)
  private final ProfileRepository profileRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final RiderProfileRepository riderProfileRepository;

<<<<<<< HEAD
  // ========== 온보딩 및 프로필 관리 ==========

  /**
   * 온보딩 완료 처리 (Profile 기반)
   */
  @Transactional
  public boolean completeOnboarding(Long userId, ProfileType selectedProfile,
      Map<String, Object> profileData) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 이미 온보딩 완료된 경우
    if (user.isOnboardingCompleted()) {
      log.warn("이미 온보딩이 완료되었습니다: userId={}", userId);
      return false;
    }

    // 1단계: Profile 먼저 생성
    Profile profile = createProfile(user, selectedProfile);

    // 2단계: 세부 프로필 생성
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
   * 프로필 전환
   */
  @Transactional
  public boolean switchProfile(Long userId, ProfileType targetProfile) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!user.isOnboardingCompleted()) {
      log.warn("온보딩이 완료되지 않은 사용자입니다: userId={}", userId);
      return false;
    }

    // 타겟 프로필 조회
    Profile targetProfileEntity = profileRepository
        .findByUserIdAndType(userId, targetProfile)
        .orElse(null);

    if (targetProfileEntity == null) {
      log.warn("해당 프로필을 찾을 수 없습니다: userId={}, targetProfile={}", userId, targetProfile);
      return false;
    }

    if (user.getCurrentActiveProfileType() == targetProfile) {
      log.info("이미 활성화된 프로필입니다: userId={}, targetProfile={}", userId, targetProfile);
      return true;
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
      return false;
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
   * 프로필 타입별 세부 프로필 생성
   */
  private boolean createDetailedProfile(Profile profile, ProfileType profileType,
      Map<String, Object> profileData) {
    switch (profileType) {
      case CUSTOMER -> {
        String nickname = (String) profileData.get("nickname");
        CustomerProfile customerProfile = CustomerProfile.builder()
            .profile(profile)
            .nickname(nickname)
            .profileImageUrl(null)
            .build();
        customerProfileRepository.save(customerProfile);
      }
      case SELLER -> {
        String nickname = (String) profileData.get("nickname");
        String businessName = (String) profileData.get("businessName");
        String businessCertificateNumber = (String) profileData.get("businessCertificateNumber");
        String businessPhoneNumber = (String) profileData.get("businessPhoneNumber");
        String bankName = (String) profileData.get("bankName");
        String accountNumber = (String) profileData.get("accountNumber");
        String accountHolder = (String) profileData.get("accountHolder");

        SellerProfile sellerProfile = SellerProfile.builder()
            .profile(profile)
            .nickname(nickname)
            .profileImageUrl(null)
            .businessName(businessName)
            .businessCertificateNumber(businessCertificateNumber)
            .businessPhoneNumber(businessPhoneNumber)
            .bankName(bankName)
            .accountNumber(accountNumber)
            .accountHolder(accountHolder)
            .build();
        sellerProfileRepository.save(sellerProfile);
      }
      case RIDER -> {
        String nickname = (String) profileData.get("nickname");
        String licenseNumber = (String) profileData.get("licenseNumber");
        String area = (String) profileData.getOrDefault("area", "서울");
        String profileImageUrl = (String) profileData.get("profileImageUrl");

        RiderProfile riderProfile = RiderProfile.builder()
            .nickname(nickname)
            .toggleStatus(RiderToggleStatus.OFF)
            .area(area)
            .profileImageUrl(profileImageUrl)
            .licenseNumber(licenseNumber)
            .bankName("")
            .bankAccountNumber("")
            .bankAccountHolderName("")
            .profile(profile)
            .build();
        riderProfileRepository.save(riderProfile);
      }
      default -> {
        log.error("지원하지 않는 프로필 타입: {}", profileType);
        return false;
      }
    }
    return true;
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
=======
  // BaseProfile 공통 필드 관리

  /**
   * Profile ID로 공통 정보 조회
   */
  public BaseProfileInfo getBaseProfileInfo(Long profileId) {
    Profile profile = findProfile(profileId);

    return switch (profile.getType()) {
      case CUSTOMER -> {
        CustomerProfile cp = findCustomerProfile(profileId);
        yield new BaseProfileInfo(cp.getNickname(), cp.getProfileImageUrl());
      }
      case SELLER -> {
        SellerProfile sp = findSellerProfile(profileId);
        yield new BaseProfileInfo(sp.getNickname(), sp.getProfileImageUrl());
      }
      case RIDER -> {
        RiderProfile rp = findRiderProfile(profileId);
        yield new BaseProfileInfo(rp.getNickname(), rp.getProfileImageUrl());
      }
    };
  }


  /**
   * Profile ID로 닉네임 업데이트 (BaseProfile 공통 메서드 활용)
   */
  @Transactional
  public void updateNickname(Long profileId, String newNickname) {
    Profile profile = findProfile(profileId);

    switch (profile.getType()) {
      case CUSTOMER -> {
        CustomerProfile cp = findCustomerProfile(profileId);
        cp.updateNickname(newNickname);
      }
      case SELLER -> {
        SellerProfile sp = findSellerProfile(profileId);
        sp.updateNickname(newNickname);
      }
      case RIDER -> {
        RiderProfile rp = findRiderProfile(profileId);
        rp.updateNickname(newNickname);
      }
    }

    log.info("닉네임 업데이트 완료 - Profile ID: {}, Type: {}, New Nickname: {}",
        profileId, profile.getType(), newNickname);
  }

  /**
   * Profile ID로 프로필 이미지 URL 업데이트 (BaseProfile 공통 메서드 활용)
   */
  @Transactional
  public void updateProfileImageUrl(Long profileId, String newProfileImageUrl) {
    Profile profile = findProfile(profileId);

    switch (profile.getType()) {
      case CUSTOMER -> {
        CustomerProfile cp = findCustomerProfile(profileId);
        cp.updateProfileImageUrl(newProfileImageUrl);
      }
      case SELLER -> {
        SellerProfile sp = findSellerProfile(profileId);
        sp.updateProfileImageUrl(newProfileImageUrl);
      }
      case RIDER -> {
        RiderProfile rp = findRiderProfile(profileId);
        rp.updateProfileImageUrl(newProfileImageUrl);
      }
    }

    log.info("프로필 이미지 업데이트 완료 - Profile ID: {}, Type: {}", profileId, profile.getType());
  }

  /**
   * Profile 활성화 (Profile.isActive = true)
   */
  @Transactional
  public void activateProfile(Long profileId) {
    Profile profile = findProfile(profileId);
    profile.activate();
    log.info("프로필 활성화 완료 - Profile ID: {}, Type: {}", profileId, profile.getType());
  }

  /**
   * Profile 비활성화 (Profile.isActive = false)
   */
  @Transactional
  public void deactivateProfile(Long profileId) {
    Profile profile = findProfile(profileId);
    profile.deactivate(); // Profile.isActive = false
    log.info("프로필 비활성화 완료 - Profile ID: {}, Type: {}", profileId, profile.getType());
  }

  /**
   * Profile 활성 상태 확인
   */
  public boolean isProfileActive(Long profileId) {
    Profile profile = findProfile(profileId);
    return profile.isActive();
  }

  // Private Helper Methods

  /**
   * Profile 조회 (공통 테이블)
   */
  private Profile findProfile(Long profileId) {
    return profileRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("프로필을 찾을 수 없습니다. Profile ID: %d", profileId)));
  }

  /**
   * CustomerProfile 조회
   */
  private CustomerProfile findCustomerProfile(Long profileId) {
    return customerProfileRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Customer 프로필을 찾을 수 없습니다. Profile ID: %d", profileId)));
  }

  /**
   * SellerProfile 조회
   */
  private SellerProfile findSellerProfile(Long profileId) {
    return sellerProfileRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Seller 프로필을 찾을 수 없습니다. Profile ID: %d", profileId)));
  }

  /**
   * RiderProfile 조회
   */
  private RiderProfile findRiderProfile(Long profileId) {
    return riderProfileRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Rider 프로필을 찾을 수 없습니다. Profile ID: %d", profileId)));
  }

  // 내부 DTO 클래스 (BaseProfile 공통 정보 DTO)
  public record BaseProfileInfo(
      String nickname,
      String profileImageUrl
  ) {

>>>>>>> 2511efe (refactor(be) : 기존 user 패키지를 auth 와 user/user , user/profile 로 분리)
  }
}