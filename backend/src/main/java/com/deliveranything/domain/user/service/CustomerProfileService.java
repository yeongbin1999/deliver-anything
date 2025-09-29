package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.address.CustomerAddress;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.CustomerAddressRepository;
import com.deliveranything.domain.user.repository.CustomerProfileRepository;
import com.deliveranything.domain.user.repository.ProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerProfileService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final CustomerAddressRepository customerAddressRepository;

  // ========== 고객 프로필 관리 ==========

  /**
   * 고객 프로필 생성 (Profile 기반)
   */
  @Transactional
  public CustomerProfile createProfile(Long userId, String nickname) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    // 이미 고객 프로필이 존재하는지 확인
    if (hasProfile(userId)) {
      log.warn("이미 고객 프로필이 존재합니다: userId={}", userId);
      return getProfile(userId);
    }

    // 1단계: Profile 생성 (전역 고유 ID)
    Profile profile = Profile.builder()
        .user(user)
        .type(ProfileType.CUSTOMER)
        .build();
    Profile savedProfile = profileRepository.save(profile);

    // 2단계: CustomerProfile 생성 (Profile ID와 동일한 ID 사용)
    CustomerProfile customerProfile = CustomerProfile.builder()
        .profile(savedProfile)
        .nickname(nickname)
        .profileImageUrl(null)
        .build();

    CustomerProfile saved = customerProfileRepository.save(customerProfile);
    log.info("고객 프로필 생성 완료: userId={}, profileId={}", userId, saved.getId());

    return saved;
  }

  /**
   * 고객 프로필 조회 (Profile ID 기반)
   */
  public CustomerProfile getProfileByProfileId(Long profileId) {
    return customerProfileRepository.findByProfileId(profileId).orElse(null);
  }

  /**
   * 고객 프로필 조회 (사용자 ID 기반)
   */
  public CustomerProfile getProfile(Long userId) {
    return customerProfileRepository.findByUserId(userId).orElse(null);
  }

  /**
   * 고객 프로필 존재 여부 확인
   */
  public boolean hasProfile(Long userId) {
    return getProfile(userId) != null;
  }

  // ========== 배송지 관리 ==========

  /**
   * 모든 배송지 조회 (Profile ID 기반)
   */
  public List<CustomerAddress> getAddressesByProfileId(Long profileId) {
    return customerAddressRepository.findAddressesByProfileId(profileId);
  }

  /**
   * 모든 배송지 조회 (사용자 ID 기반)
   */
  public List<CustomerAddress> getAddresses(Long userId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: userId={}", userId);
      return List.of();
    }

    return customerAddressRepository.findAddressesByProfile(profile);
  }

  /**
   * 특정 배송지 조회 (권한 체크 포함)
   */
  public CustomerAddress getAddress(Long userId, Long addressId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    CustomerAddress address = customerAddressRepository.findById(addressId).orElse(null);
    if (address == null) {
      log.warn("배송지를 찾을 수 없습니다: addressId={}", addressId);
      return null;
    }

    // 권한 체크: 해당 사용자의 배송지인지 확인
    if (!address.getCustomerProfile().getId().equals(profile.getId())) {
      log.warn("배송지 접근 권한이 없습니다: userId={}, addressId={}", userId, addressId);
      return null;
    }

    return address;
  }

  /**
   * Profile ID로 특정 배송지 조회
   */
  public CustomerAddress getAddressByProfileId(Long profileId, Long addressId) {
    CustomerProfile profile = getProfileByProfileId(profileId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: profileId={}", profileId);
      return null;
    }

    CustomerAddress address = customerAddressRepository.findById(addressId).orElse(null);
    if (address == null) {
      log.warn("배송지를 찾을 수 없습니다: addressId={}", addressId);
      return null;
    }

    // 권한 체크
    if (!address.getCustomerProfile().getId().equals(profileId)) {
      log.warn("배송지 접근 권한이 없습니다: profileId={}, addressId={}", profileId, addressId);
      return null;
    }

    return address;
  }

  /**
   * 배송지 추가
   */
  @Transactional
  public CustomerAddress addAddress(Long userId, String addressName, String address,
      Double latitude, Double longitude) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    CustomerAddress customerAddress = CustomerAddress.builder()
        .customerProfile(profile)
        .addressName(addressName)
        .address(address)
        .latitude(latitude)
        .longitude(longitude)
        .build();

    CustomerAddress saved = customerAddressRepository.save(customerAddress);
    log.info("배송지 추가 완료: userId={}, profileId={}, addressId={}",
        userId, profile.getId(), saved.getId());

    // 첫 번째 주소는 자동으로 기본 설정
    if (profile.getDefaultAddressId() == null) {
      profile.updateDefaultAddressId(saved.getId());
      customerProfileRepository.save(profile);
      log.info("첫 번째 주소를 기본 배송지로 설정: profileId={}, addressId={}",
          profile.getId(), saved.getId());
    }

    return saved;
  }

  /**
   * Profile ID로 배송지 추가
   */
  @Transactional
  public CustomerAddress addAddressByProfileId(Long profileId, String addressName, String address,
      Double latitude, Double longitude) {
    CustomerProfile profile = getProfileByProfileId(profileId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: profileId={}", profileId);
      return null;
    }

    CustomerAddress customerAddress = CustomerAddress.builder()
        .customerProfile(profile)
        .addressName(addressName)
        .address(address)
        .latitude(latitude)
        .longitude(longitude)
        .build();

    CustomerAddress saved = customerAddressRepository.save(customerAddress);
    log.info("배송지 추가 완료: profileId={}, addressId={}", profileId, saved.getId());

    // 첫 번째 주소는 자동으로 기본 설정
    if (profile.getDefaultAddressId() == null) {
      profile.updateDefaultAddressId(saved.getId());
      customerProfileRepository.save(profile);
    }

    return saved;
  }

  /**
   * 배송지 수정
   */
  @Transactional
  public boolean updateAddress(Long userId, Long addressId, String addressName, String address,
      Double latitude, Double longitude) {
    CustomerAddress customerAddress = getAddress(userId, addressId);
    if (customerAddress == null) {
      return false;
    }

    customerAddress.updateAddress(addressName, address, latitude, longitude);
    customerAddressRepository.save(customerAddress);

    log.info("배송지 수정 완료: userId={}, addressId={}", userId, addressId);
    return true;
  }

  /**
   * 배송지 삭제
   */
  @Transactional
  public boolean deleteAddress(Long userId, Long addressId) {
    CustomerAddress customerAddress = getAddress(userId, addressId);
    if (customerAddress == null) {
      return false;
    }

    // 기본 배송지인 경우 삭제 불가 처리
    if (customerAddress.isDefault()) {
      log.warn("기본 배송지는 삭제할 수 없습니다: userId={}, addressId={}", userId, addressId);
      return false;
    }

    customerAddressRepository.delete(customerAddress);
    log.info("배송지 삭제 완료: userId={}, addressId={}", userId, addressId);
    return true;
  }

  /**
   * 기본 배송지 설정
   */
  @Transactional
  public boolean setDefaultAddress(Long userId, Long addressId) {
    CustomerProfile profile = getProfile(userId);
    CustomerAddress customerAddress = getAddress(userId, addressId);
    if (customerAddress == null) {
      return false;
    }

    // CustomerProfile 엔티티의 기본 주소 ID 업데이트
    profile.updateDefaultAddressId(addressId);
    customerProfileRepository.save(profile);

    log.info("기본 배송지 설정 완료: userId={}, profileId={}, addressId={}",
        userId, profile.getId(), addressId);
    return true;
  }

  /**
   * 현재 기본 배송지 조회
   */
  public CustomerAddress getCurrentAddress(Long userId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null || profile.getDefaultAddressId() == null) {
      return null;
    }

    return customerAddressRepository.findById(profile.getDefaultAddressId()).orElse(null);
  }

  /**
   * Profile ID로 현재 기본 배송지 조회
   */
  public CustomerAddress getCurrentAddressByProfileId(Long profileId) {
    CustomerProfile profile = getProfileByProfileId(profileId);
    if (profile == null || profile.getDefaultAddressId() == null) {
      return null;
    }

    return customerAddressRepository.findById(profile.getDefaultAddressId()).orElse(null);
  }

  /**
   * 사용자의 기본 배송지 ID 조회
   */
  public Long getDefaultAddressId(Long userId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      return null;
    }
    return profile.getDefaultAddressId();
  }

  /**
   * 배송지 개수 조회
   */
  public long countAddresses(Long userId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      return 0;
    }
    return customerAddressRepository.countByCustomerProfile(profile);
  }

  /**
   * Profile ID로 배송지 개수 조회
   */
  public long countAddressesByProfileId(Long profileId) {
    return customerAddressRepository.countByCustomerProfileId(profileId);
  }

  /**
   * 배송지가 기본 배송지인지 확인
   */
  public boolean isDefaultAddress(Long userId, Long addressId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null || profile.getDefaultAddressId() == null) {
      return false;
    }
    return profile.getDefaultAddressId().equals(addressId);
  }
}
