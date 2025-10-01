package com.deliveranything.domain.auth.service;

import com.deliveranything.domain.auth.entity.RefreshToken;
import com.deliveranything.domain.auth.repository.RefreshTokenRepository;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

  private final AuthTokenService authTokenService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  /**
   * JWT Access Token 생성
   */
  public String genAccessToken(User user) {
    return authTokenService.genAccessToken(user);
  }

  /**
   * RefreshToken 생성
   */
  @Transactional
  public RefreshToken genRefreshToken(User user, String deviceInfo) {
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
   * 모든 RefreshToken 무효화 (로그아웃)
   */
  @Transactional
  public void invalidateAllRefreshTokens(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    refreshTokenRepository.deactivateAllTokensByUser(user);
    log.info("모든 RefreshToken 무효화: userId={}", userId);
  }

  /**
   * Refresh Token으로 Access Token 재발급
   */
  public String refreshAccessToken(String refreshTokenValue) {
    User user = getUserByRefreshToken(refreshTokenValue);
    return genAccessToken(user);
  }
}