package com.deliveranything.domain.auth.service;

import com.deliveranything.domain.auth.entity.RefreshToken;
import com.deliveranything.domain.auth.enums.SocialProvider;
import com.deliveranything.domain.user.profile.dto.SwitchProfileResponse;
import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.rerpository.ProfileRepository;
import com.deliveranything.domain.user.profile.service.ProfileService;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final TokenService tokenService;
  private final ProfileService profileService;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원가입 (멀티 프로필 지원)
   */
  @Transactional
  public User signup(String email, String password, String name, String phoneNumber) {
    // 중복 체크
    if (userRepository.existsByEmail(email)) {
      log.warn("이미 존재하는 이메일: {}", email);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    if (userRepository.existsByPhoneNumber(phoneNumber)) {
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
  public LoginResult login(String email, String password, String deviceInfo) {
    User user = userRepository.findByEmail(email)
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

    // 토큰 발급
    String accessToken = tokenService.genAccessToken(user);
    RefreshToken refreshToken = tokenService.genRefreshToken(user, deviceInfo);

    return new LoginResult(user, accessToken, refreshToken.getTokenValue());
  }

  /**
   * 로그아웃
   */
  @Transactional
  public void logout(Long userId) {
    tokenService.invalidateAllRefreshTokens(userId);
  }

  /**
   * 이메일 인증
   */
  @Transactional
  public void verifyEmail(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.verifyEmail();
    userRepository.save(user);

    log.info("이메일 인증 완료: userId={}", userId);
  }

  /**
   * 프로필 전환 + 토큰 재발급 (Orchestration)
   */
  @Transactional
  public SwitchProfileResponse switchProfileWithTokenReissue(
      Long userId,
      ProfileType targetProfileType) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
      String newAccessToken = tokenService.genAccessToken(user);

      return SwitchProfileResponse.builder()
          .userId(userId)
          .previousProfileType(previousProfileType)
          .previousProfileId(previousProfileId)
          .currentProfileType(targetProfileType)
          .currentProfileId(targetProfile.getId())
          .accessToken(newAccessToken)
          .build();
    }

    // 프로필 전환 (ProfileService 위임)
    boolean switched = profileService.switchProfile(userId, targetProfileType);
    if (!switched) {
      log.warn("프로필 전환 실패: userId={}, targetProfile={}", userId, targetProfileType);
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 전환된 user로 새 토큰 생성
    User updatedUser = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    String newAccessToken = tokenService.genAccessToken(updatedUser);

    // 내부 DTO로 반환 (토큰 포함)
    return SwitchProfileResponse.builder()
        .userId(userId)
        .previousProfileType(previousProfileType)
        .previousProfileId(previousProfileId)
        .currentProfileType(targetProfileType)
        .currentProfileId(targetProfile.getId())
        .accessToken(newAccessToken)
        .build();
  }

  // 내부 DTO
  public record LoginResult(
      User user,
      String accessToken,
      String refreshToken
  ) {

  }
}