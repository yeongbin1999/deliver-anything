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

}
