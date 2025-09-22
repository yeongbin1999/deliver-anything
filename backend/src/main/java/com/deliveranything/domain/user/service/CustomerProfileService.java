package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.address.CustomerAddress;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.repository.CustomerAddressRepository;
import com.deliveranything.domain.user.repository.CustomerProfileRepository;
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
  private final CustomerProfileRepository customerProfileRepository;
  private final CustomerAddressRepository customerAddressRepository;

// 고객 프로필 관리

  // 고객 프로필 생성 (UserService에서 온보딩 시 호출 예정)
  @Transactional
  public CustomerProfile createProfile(Long userId, String nickname) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    // 이미 고객 프로필이 존재하는 경우
    if (user.getCustomerProfile() != null) {
      log.warn("이미 고객 프로필이 존재합니다: userId={}", userId);
      return user.getCustomerProfile();
    }

    CustomerProfile customerProfile = CustomerProfile.builder()
        .user(user)
        .nickname(nickname)
        .profileImageUrl(null)  // 기본값
        .build();

    customerProfileRepository.save(customerProfile);
    log.info("고객 프로필 생성 완료: userId={}, profileId={}", userId, customerProfile.getId());

    return customerProfile;
  }

  //고객 프로필 조회
  public CustomerProfile getProfile(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    CustomerProfile profile = user.getCustomerProfile();
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다: userId={}", userId);
      return null;
    }

    return profile;
  }

  // 고객 프로필 존재 여부 확인
  public boolean hasProfile(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return false;
    }
    return user.getCustomerProfile() != null;
  }

  // 고객 프로필 수정
  @Transactional
  public boolean updateProfile(Long userId, String nickname, String profileImageUrl) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      return false;
    }

    profile.updateProfile(nickname, profileImageUrl);
    customerProfileRepository.save(profile);

    log.info("고객 프로필 수정 완료: userId={}, nickname={}", userId, nickname);
    return true;
  }

  // 배송지 관리

  // 모든 배송지 조회
  public List<CustomerAddress> getAddresses(Long userId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다.: userId={}", userId);
      return List.of();
    }

    return customerAddressRepository.findAddressesByProfile(profile);
  }

  // 특정 배송지 조회
  public CustomerAddress getAddress(Long userId, Long addressId) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다.: userId={}", userId);
      return null;
    }

    CustomerAddress address = customerAddressRepository.findById(addressId).orElse(null);
    if (address == null) {
      log.warn("배송지를 찾을 수 없습니다: addressId={}", addressId);
      return null;
    }

    // 권한 체크 : 해당 사용자의 배송지인지 확인
    if (!address.getCustomerProfile().getId().equals(profile.getId())) {
      log.warn("배송지 접근 권한이 없습니다: userId={}, addressId={}", userId, addressId);
      return null;
    }

    return address;
  }

  // 배송지 추가
  @Transactional
  public CustomerAddress addAddress(Long userId, String addressName, String address,
      Double latitude, Double longitude) {
    CustomerProfile profile = getProfile(userId);
    if (profile == null) {
      log.warn("고객 프로필을 찾을 수 없습니다.: userId={}", userId);
      return null;
    }

    CustomerAddress customerAddress = CustomerAddress.builder()
        .customerProfile(profile)
        .addressName(addressName)
        .address(address)
        .latitude(latitude)
        .longitude(longitude)
        .build();

    customerAddressRepository.save(customerAddress);
    log.info("배송지 추가 완료: userId={}, addressId={}", userId, customerAddress.getId());

    return customerAddress;
  }

  // 배송지 수정
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

  // 배송지 삭제
  @Transactional
  public boolean deleteAddress(Long userId, Long addressId) {
    CustomerAddress customerAddress = getAddress(userId, addressId);
    if (customerAddress == null) {
      return false;
    }

    /**
     * 기본 배송지인 경우 삭제 불가 처리
     * 사용자 플로우:
     * 1. 기본 배송지 삭제 시도
     * 2. 서비스에서 기본 배송지 삭제 불가 응답 ("다른 주소를 먼저 기본으로 설정해주세요")
     * 3. 사용자가 다른 주소를 기본 배송지로 설정
     * 4. 이후 다시 기본 배송지였던 기존 주소 삭제 시도 가능
     * -> 비즈니스 연속성과 사용자 경험 최적화(실수 방지 등)
     */
    if (customerAddress.isDefault()) {
      log.warn("기본 배송지는 삭제할 수 없습니다: userId={}, addressId={}", userId, addressId);
      return false;
    }

    customerAddressRepository.delete(customerAddress);
    log.info("배송지 삭제 완료: userId={}, addressId={}", userId, addressId);
    return true;
  }

  // 기본 배송지 설정
  @Transactional
  public boolean setDefaultAddress(Long userId, Long addressId) {
    CustomerAddress customerAddress = getAddress(userId, addressId);
    if (customerAddress == null) {
      return false;
    }

    // User 엔티티의 기본 주소 ID 업데이트 ( CustomerAddress -> CustomerProfiel -> User의 default_address_id 참조...인데 기본 배송지 주소를 User가 가지기보단 CustomerProfile이 가지는게 더 맞지 않는 것 같아 수정 예정)
    customerAddress.setAsDefault();

    log.info("기본 배송지 설정 완료: userId={}, addressId={}", userId, addressId);
    return true;
  }

}
