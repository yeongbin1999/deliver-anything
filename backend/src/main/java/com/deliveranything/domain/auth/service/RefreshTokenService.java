package com.deliveranything.domain.auth.service;

import com.deliveranything.domain.auth.dto.RefreshTokenDto;
import com.deliveranything.domain.auth.repository.RefreshTokenRepository;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

  private final AccessTokenService accessTokenService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Value("${custom.refreshToken.expirationDays}")
  private int refreshTokenExpirationDays;


  /**
   * RefreshToken 생성 (Redis 저장)
   */
  @Transactional
  public String genRefreshToken(User user, String deviceInfo) {
    // 1. 기존 디바이스 토큰 삭제 (Redis)
    refreshTokenRepository.deleteByUserAndDevice(user.getId(), deviceInfo);

    // 2. 새 토큰 생성
    String tokenValue = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays);

    RefreshTokenDto redisToken = RefreshTokenDto.builder()
        .userId(user.getId())
        .tokenValue(tokenValue)
        .deviceInfo(deviceInfo)
        .expiresAt(expiresAt)
        .createdAt(LocalDateTime.now())
        .build();

    // 3. Redis에 저장
    refreshTokenRepository.save(redisToken);

    log.info("RefreshToken 생성 (Redis): userId={}, deviceInfo={}",
        user.getId(), deviceInfo);

    return tokenValue;
  }

  /**
   * RefreshToken으로 사용자 조회 (Redis)
   */
  public User getUserByRefreshToken(String refreshTokenValue) {
    // 1. Redis에서 토큰 조회 (인덱스 사용)
    RefreshTokenDto redisToken = refreshTokenRepository
        .findByTokenValue(refreshTokenValue)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 2. 유효성 검증
    if (!redisToken.isValid()) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 3. 사용자 조회
    return userRepository.findById(redisToken.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  /**
   * 특정 디바이스의 RefreshToken 무효화 (로그아웃)
   */
  @Transactional
  public void invalidateRefreshToken(Long userId, String deviceInfo) {
    refreshTokenRepository.deleteByUserAndDevice(userId, deviceInfo);
    log.info("RefreshToken 무효화 (Redis): userId={}, deviceInfo={}", userId, deviceInfo);
  }

  /**
   * 모든 기기의 RefreshToken 무효화 (전체 로그아웃)
   */
  @Transactional
  public void invalidateAllRefreshTokens(Long userId) {
    refreshTokenRepository.deleteAllByUser(userId);
    log.info("모든 RefreshToken 무효화 (Redis): userId={}", userId);
  }

  /**
   * Refresh Token으로 Access Token 재발급
   */
  public String refreshAccessToken(String refreshTokenValue) {
    User user = getUserByRefreshToken(refreshTokenValue);
    return accessTokenService.genAccessToken(user);
  }

}