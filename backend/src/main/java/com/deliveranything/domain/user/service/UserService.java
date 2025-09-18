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
      return false;
    }
    return canSwitchToProfileInternal(user, targetProfile);
  }

  // 온보딩 관련 Methods
  @Transactional
  public boolean completeOnboarding(Long userId, ProfileType selectedProfile) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return false;
    }

    // 이미 온보딩 완료된 경우
    if (user.isOnboardingCompleted()) {
      log.warn("이미 온보딩이 완료되었습니다: userId={}", userId);
      return false;
    }

    // 선택한 프로필이 존재하는지 확인 (Service에서 직접 처리)
    if (!hasProfileInternal(user, selectedProfile)) {
      log.warn("해당 프로필을 찾을 수 없습니다: userId={}, selectedProfile={}", userId, selectedProfile);
      return false;
    }

    // 단순 상태 변경
    user.completeOnboarding(selectedProfile);
    userRepository.save(user);

    log.info("온보딩 완료: userId={}, selectedProfile={}", userId, selectedProfile);
    return true;
  }

  public boolean isOnboardingCompleted(Long userId) {
    User user = findById(userId);
    if (user == null) {
      return false;
    }
    return user.isOnboardingCompleted();
  }

  // 기타 Methods
  @Transactional
  public void updateLastLoginAt(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }

    user.updateLastLoginAt();
    userRepository.save(user);

    log.info("사용자 마지막 로그인 시간 업데이트 완료: userId={}", userId);
  }

  // 기본주소 설정
  public boolean setDefaultAddress(Long userId, Long addressId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return false;
    }
    // 소비자 프로필 존재 확인
    if (!hasProfileInternal(user, ProfileType.CUSTOMER)) {
      log.warn("소비자 프로필을 찾을 수 없습니다: userId={}", userId);
      return false;
    }

    user.setDefaultAddress(addressId);
    userRepository.save(user);

    log.info("기본 주소 설정 완료: userId={}, addressId={}", userId, addressId);
    return true;
  }

  // 이메일 인증 처리 임시
  @Transactional
  public void verifyEmail(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }

    user.verifyEmail();
    userRepository.save(user);

    log.info("이메일 인증 완료: userId={}", userId);
  }

  // 회원 가입 - 임시 (추후 이메일 인증, SocialProvider, SocialId 등 추가 고려)
  public User signup(String email, String password, String name, String phoneNumber) {
    // 중복 체크
    if (existsByEmail(email)) {
      log.warn("이미 존재하는 이메일: {}", email);
      return null;
    }

    if (existsByPhoneNumber(phoneNumber)) {
      log.warn("이미 존재하는 전화번호: {}", phoneNumber);
      return null;
    }

    User newUser = User.builder()
        .email(email)
        .password(password)
        .name(name)
        .phoneNumber(phoneNumber)
        .build();

    userRepository.save(newUser);
    log.info("신규 사용자 가입 완료: userId={}", newUser.getId());
    return newUser;
  }

  // 계정 활성화/비활성화 ( Spring Security의 UserDetailsService 연동 대비)
  @Transactional
  public void enableUser(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }

    user.enable();
    userRepository.save(user);
    log.info("계정 활성화: userId={}", userId);
  }

  @Transactional
  public void disableUser(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }

    user.disable();
    userRepository.save(user);
    log.info("계정 비활성화: userId={}", userId);
  }

  public boolean isUserEnabled(Long userId) {
    User user = findById(userId);
    if (user == null) {
      return false;
    }
    return user.isEnabled();
  }

  // 회원탈퇴 (일단 논리적 삭제)
  @Transactional
  public boolean deleteUser(Long userId) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return false;
    }

    // 계정 비활성화 (논리적 삭제)
    user.disable();
    userRepository.save(user);

    log.info("회원 탈퇴 처리 완료: userId={}", userId);
    return true;
  }

  // 로그인 기능 (임시 - Spring Security 도입 전)
  public User login(String email, String password) {
    User user = findByEmail(email);
    if (user == null) {
      log.warn("존재하지 않는 이메일: {}", email);
      return null;
    }

    if (!user.isEnabled()) {
      log.warn("비활성화된 계정 로그인 시도: email={}", email);
      return null;
    }

    // TODO: Spring Security 도입 후 PasswordEncoder로 암호화된 비밀번호 비교
    if (!user.getPassword().equals(password)) {
      log.warn("잘못된 비밀번호 로그인 시도: email={}", email);
      return null;
    }

    // 로그인 성공 - 마지막 로그인 시간 업데이트
    updateLastLoginAt(user.getId());

    log.info("로그인 성공: userId={}, email={}", user.getId(), email);
    return user;
  }

  // 프로필 관리를 위한 Private Helper Methods
  private List<ProfileType> getAvailableProfilesInternal(User user) {
    List<ProfileType> profiles = new ArrayList<>();

    if (user.getCustomerProfile() != null) {
      profiles.add(ProfileType.CUSTOMER);
    }
    // TODO: SellerProfile, RiderProfile 연관관계 추가 시 로직 확장

    return profiles;
  }


  private boolean canSwitchToProfileInternal(User user, ProfileType targetProfile) {
    if (!user.isOnboardingCompleted()) {
      return false;
    }

    return switch (targetProfile) {
      case CUSTOMER -> user.getCustomerProfile() != null;
      case SELLER -> false; // TODO: 나중에 SellerProfile 연관관계 추가 시 수정
      case RIDER -> false;  // TODO: 나중에 RiderProfile 연관관계 추가 시 수정
    };
  }

  private boolean hasProfileInternal(User user, ProfileType profileType) {
    return switch (profileType) {
      case CUSTOMER -> user.getCustomerProfile() != null;
      case SELLER -> false; // TODO: 나중에 수정
      case RIDER -> false;  // TODO: 나중에 수정
    };
  }

}