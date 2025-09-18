package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  // 기본 조회 Methods
  public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public Optional<User> findByIdOptional(Long id) {
    return userRepository.findById(id);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  // 중복 체크 Methods
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return userRepository.existsByPhoneNumber(phoneNumber);
  }

  // 기본 정보 수정 Methods
  @Transactional
  public void updateBasicInfo(Long userId, String name, String phoneNumber) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }
    // 핸드폰 번호 중복 체크 (본인 제외)
    if (!user.getPhoneNumber().equals(phoneNumber) &&
        existsByPhoneNumber(phoneNumber)) {
      log.warn("이미 사용 중인 전화번호입니다: phoneNumber={}", phoneNumber);
      return;
    }

    user.updateBasicInfo(name, phoneNumber);
    userRepository.save(user);

    log.info("사용자 기본 정보 업데이트 완료: userId={}", userId);
  }

  @Transactional
  public void updatePassword(Long userId, String newPassword) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }
    user.updatePassword(newPassword);
    userRepository.save(user);
    log.info("사용자 비밀번호 업데이트 완료: userId={}", userId);
  }

  // 프로필 관리 Methods
  @Transactional
  public boolean switchProfile(Long userId, ProfileType targetProfile) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return false;
    }
    if (!user.isOnboardingCompleted()) {
      log.warn("온보딩이 완료되지 않은 사용자입니다: userId={}", userId);
      return false;
    }
    if (user.getCurrentActiveProfile() == targetProfile) {
      log.info("이미 활성화된 프로필입니다: userId={}, targetProfile={}", userId, targetProfile);
      return true;
    }
    user.switchProfile(targetProfile);
    userRepository.save(user);

    log.info("프로필 전환 완료: userId={}, newActiveProfile={}", userId, targetProfile);
    return true;
  }

  public List<ProfileType> getAvailableProfiles(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return List.of();
    }

    // Service에서 직접 처리
    return getAvailableProfilesInternal(user);
  }

  public boolean canSwitchToProfile(Long userId, ProfileType targetProfile) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return false;
    }
    if (!user.isOnboardingCompleted()) {
      log.warn("온보딩이 완료되지 않은 사용자입니다: userId={}", userId);
      return false;
    }
    List<ProfileType> availableProfiles = getAvailableProfilesInternal(user);
    return availableProfiles.contains(targetProfile);
  }

  // 프로필 관리를 위한 Private Helper Methods
  private List<ProfileType> getAvailableProfilesInternal(User user) {
    List<ProfileType> profiles = new ArrayList<>();

    if (user.getCustomerProfile() != null) {
      profiles.add(ProfileType.CONSUMER);
    }
    // TODO: SellerProfile, RiderProfile 연관관계 추가 시 로직 확장

    return profiles;
  }

}
