package com.deliveranything.domain.user.profile.service;

import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.entity.SellerProfile;
import com.deliveranything.domain.user.profile.rerpository.CustomerProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.ProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.RiderProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.SellerProfileRepository;
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

  private final ProfileRepository profileRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final RiderProfileRepository riderProfileRepository;

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

  }
}