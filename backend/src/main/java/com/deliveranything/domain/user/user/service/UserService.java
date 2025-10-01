package com.deliveranything.domain.user.user.service;

import com.deliveranything.domain.auth.entity.RefreshToken;
import com.deliveranything.domain.auth.enums.SocialProvider;
import com.deliveranything.domain.auth.repository.RefreshTokenRepository;
import com.deliveranything.domain.auth.service.AuthTokenService;
import com.deliveranything.domain.user.profile.dto.SwitchProfileResponse;
import com.deliveranything.domain.user.profile.entity.CustomerProfile;
import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.entity.RiderProfile;
import com.deliveranything.domain.user.profile.entity.SellerProfile;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.enums.RiderToggleStatus;
import com.deliveranything.domain.user.profile.rerpository.CustomerProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.ProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.RiderProfileRepository;
import com.deliveranything.domain.user.profile.rerpository.SellerProfileRepository;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final RiderProfileRepository riderProfileRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthTokenService authTokenService;
  private final PasswordEncoder passwordEncoder;

  // ========== 기본 사용자 관리 ==========

  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Optional<User> findByApiKey(String apiKey) {
    return userRepository.findByApiKey(apiKey);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return userRepository.existsByPhoneNumber(phoneNumber);
  }

  public long count() {
    return userRepository.count();
  }

  // ========== 회원가입 및 인증 ==========

  /**
   * 회원가입 (멀티 프로필 지원)
   */
  @Transactional
  public User signup(String email, String password, String name, String phoneNumber) {
    // 중복 체크
    if (existsByEmail(email)) {
      log.warn("이미 존재하는 이메일: {}", email);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    if (existsByPhoneNumber(phoneNumber)) {
      log.warn("이미 존재하는 전화번호: {}", phoneNumber);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(password);

    User newUser = User.builder()
        .email(email)
        .password(encodedPassword)
        .name(name)
        .phoneNumber(phoneNumber)
        .socialProvider(SocialProvider.LOCAL)
        .build();

    User savedUser = userRepository.save(newUser);
    log.info("신규 사용자 가입 완료: userId={}, email={}", savedUser.getId(), email);

    return savedUser;
  }

  /**
   * 로그인
   */
  @Transactional
  public User login(String email, String password) {
    User user = findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!user.isEnabled()) {
      log.warn("비활성화된 계정 로그인 시도: email={}", email);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 비밀번호 검증
    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("잘못된 비밀번호 로그인 시도: email={}", email);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 마지막 로그인 시간 업데이트
    user.updateLastLoginAt();
    userRepository.save(user);

    log.info("로그인 성공: userId={}, email={}", user.getId(), email);
    return user;
  }

  /**
   * JWT Access Token 생성
   */
  public String genAccessToken(User user) {
    return authTokenService.genAccessToken(user);
  }

  /**
   * JWT 페이로드 파싱
   */
  public Map<String, Object> payload(String accessToken) {
    return authTokenService.payload(accessToken);
  }

  /**
   * RefreshToken으로 사용자 조회
   */
  public User getUserByRefreshToken(String refreshTokenValue) {
    RefreshToken refreshToken = refreshTokenRepository
        .findByTokenValueAndIsActiveTrue(refreshTokenValue)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!refreshToken.isValid()) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    return refreshToken.getUser();
  }

  /**
   * 비밀번호 변경 (현재 비밀번호 검증 포함)
   */
  @Transactional
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user = findById(userId);

    // 현재 비밀번호 검증
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    String encodedNewPassword = passwordEncoder.encode(newPassword);
    user.updatePassword(encodedNewPassword);
    userRepository.save(user);

    log.info("사용자 비밀번호 변경 완료: userId={}", userId);
  }

  // ========== 멀티 프로필 온보딩 ==========

  /**
   * 온보딩 완료 처리 (Profile 기반)
   */
  @Transactional
  public boolean completeOnboarding(Long userId, ProfileType selectedProfile,
      Map<String, Object> profileData) {
    User user = findById(userId);

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
    User user = findById(userId);

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
    User user = findById(userId);
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
        String profileImageUrl = (String) profileData.get("profileImageUrl"); // BaseProfile 필드

        RiderProfile riderProfile = RiderProfile.builder()
            .nickname(nickname)
            .toggleStatus(RiderToggleStatus.OFF)
            .area(area)
            .profileImageUrl(profileImageUrl)
            .licenseNumber(licenseNumber)
            .bankName("")
            .bankAccountNumber("")
            .bankAccountHolderName("")
            // Profile 객체 전달 (RiderProfile의 @MapsId가 사용하는 필드)
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

  // ========== RefreshToken 관리 ==========

  /**
   * RefreshToken 생성
   */
  @Transactional
  public RefreshToken createRefreshToken(User user, String deviceInfo) {
    // 해당 디바이스의 기존 토큰 비활성화
    refreshTokenRepository.deactivateTokenByUserAndDevice(user, deviceInfo);

    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .expiresAt(LocalDateTime.now().plusDays(30)) // 30일 유효
        .deviceInfo(deviceInfo)
        .build();

    RefreshToken saved = refreshTokenRepository.save(refreshToken);
    log.info("RefreshToken 생성: userId={}, deviceInfo={}", user.getId(), deviceInfo);

    return saved;
  }

  /**
   * 모든 RefreshToken 무효화 (로그아웃)
   */
  @Transactional
  public void invalidateAllRefreshTokens(Long userId) {
    User user = findById(userId);

    refreshTokenRepository.deactivateAllTokensByUser(user);
    log.info("모든 RefreshToken 무효화: userId={}", userId);
  }

  // ========== 계정 관리 ==========

  @Transactional
  public void updatePassword(Long userId, String newPassword) {
    User user = findById(userId);

    String encodedPassword = passwordEncoder.encode(newPassword);
    user.updatePassword(encodedPassword);
    userRepository.save(user);

    log.info("사용자 비밀번호 업데이트 완료: userId={}", userId);
  }

  @Transactional
  public void verifyEmail(Long userId) {
    User user = findById(userId);

    user.verifyEmail();
    userRepository.save(user);

    log.info("이메일 인증 완료: userId={}", userId);
  }

  // ========== 프로필 조회 헬퍼 ==========

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
    User user = findById(userId);
    return profileRepository.findActiveProfilesByUser(user);
  }

  /**
   * 프로필 전환 + 토큰 재발급
   */

  @Transactional
  public SwitchProfileResponse switchProfileWithTokenReissue(
      Long userId,
      ProfileType targetProfileType) {

    User user = findById(userId);

    if (!user.isOnboardingCompleted()) {
      log.warn("온보딩이 완료되지 않은 사용자입니다: userId={}", userId);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 현재 프로필 정보 저장
    ProfileType previousProfileType = user.getCurrentActiveProfileType();
    Long previousProfileId = user.getCurrentActiveProfileId();

    // 타겟 프로필 조회
    Profile targetProfile = profileRepository
        .findByUserIdAndType(userId, targetProfileType)
        .orElseThrow(() -> {
          log.warn("해당 프로필을 찾을 수 없습니다: userId={}, targetProfile={}",
              userId, targetProfileType);
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        });

    // 이미 활성화된 프로필인 경우
    if (user.getCurrentActiveProfileType() == targetProfileType) {
      log.info("이미 활성화된 프로필입니다: userId={}, targetProfile={}",
          userId, targetProfileType);

      // 그래도 토큰은 재발급
      String newAccessToken = genAccessToken(user);

      return SwitchProfileResponse.builder()
          .userId(userId)
          .previousProfileType(previousProfileType)
          .previousProfileId(previousProfileId)
          .currentProfileType(targetProfileType)
          .currentProfileId(targetProfile.getId())
          .accessToken(newAccessToken)
          .build();
    }

    // 프로필 전환 시도
    try {
      user.switchProfile(targetProfile);
      userRepository.save(user);  // 변경사항 저장
    } catch (IllegalStateException e) {
      log.warn("프로필 전환 실패: userId={}, targetProfile={}, error={}",
          userId, targetProfileType, e.getMessage());
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    // 전환된 user로 새 토큰 생성
    String newAccessToken = genAccessToken(user);

    // 내부 DTO로 반환 (토큰 포함)
    return SwitchProfileResponse.builder()
        .userId(userId)
        .previousProfileType(previousProfileType)
        .previousProfileId(previousProfileId)
        .currentProfileType(targetProfileType)
        .currentProfileId(targetProfile.getId())
        .accessToken(newAccessToken)        // Controller로 전달
        .build();
  }
}