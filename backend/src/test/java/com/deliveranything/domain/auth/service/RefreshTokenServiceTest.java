package com.deliveranything.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deliveranything.domain.auth.dto.RedisRefreshTokenDto;
import com.deliveranything.domain.auth.repository.RedisRefreshTokenRepository;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 단위 테스트")
class RefreshTokenServiceTest {

  @Mock
  private AccessTokenService accessTokenService;

  @Mock
  private RedisRefreshTokenRepository redisRefreshTokenRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private RefreshTokenService refreshTokenService;

  @Nested
  @DisplayName("Refresh Token 생성 테스트")
  class GenRefreshTokenTest {

    @Test
    @DisplayName("성공 - Refresh Token 생성")
    void genRefreshToken_success() {
      // Given
      ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 7);

      User mockUser = mock(User.class);
      when(mockUser.getId()).thenReturn(1L);

      String deviceInfo = "Chrome";

      doNothing().when(redisRefreshTokenRepository).deleteByUserAndDevice(1L, deviceInfo);
      doNothing().when(redisRefreshTokenRepository).save(any(RedisRefreshTokenDto.class));

      // When
      String token = refreshTokenService.genRefreshToken(mockUser, deviceInfo);

      // Then
      assertNotNull(token);
      verify(redisRefreshTokenRepository, times(1)).deleteByUserAndDevice(1L, deviceInfo);
      verify(redisRefreshTokenRepository, times(1)).save(any(RedisRefreshTokenDto.class));
    }
  }

  @Nested
  @DisplayName("Refresh Token으로 사용자 조회 테스트")
  class GetUserByRefreshTokenTest {

    @Test
    @DisplayName("성공 - 유효한 Refresh Token")
    void getUserByRefreshToken_success() {
      // Given
      String tokenValue = "valid-token";
      Long userId = 1L;

      RedisRefreshTokenDto mockToken = mock(RedisRefreshTokenDto.class);
      when(mockToken.getUserId()).thenReturn(userId);
      when(mockToken.isValid()).thenReturn(true);

      User mockUser = mock(User.class);
      when(mockUser.getId()).thenReturn(userId);

      when(redisRefreshTokenRepository.findByTokenValue(tokenValue))
          .thenReturn(Optional.of(mockToken));
      when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

      // When
      User result = refreshTokenService.getUserByRefreshToken(tokenValue);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.getId());
      verify(redisRefreshTokenRepository, times(1)).findByTokenValue(tokenValue);
      verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("실패 - 토큰을 찾을 수 없음")
    void getUserByRefreshToken_fail_not_found() {
      // Given
      String tokenValue = "invalid-token";
      when(redisRefreshTokenRepository.findByTokenValue(tokenValue))
          .thenReturn(Optional.empty());

      // When & Then
      assertThrows(CustomException.class, () -> {
        refreshTokenService.getUserByRefreshToken(tokenValue);
      });
    }

    @Test
    @DisplayName("실패 - 만료된 토큰")
    void getUserByRefreshToken_fail_expired() {
      // Given
      String tokenValue = "expired-token";

      RedisRefreshTokenDto mockToken = mock(RedisRefreshTokenDto.class);
      when(mockToken.isValid()).thenReturn(false);

      when(redisRefreshTokenRepository.findByTokenValue(tokenValue))
          .thenReturn(Optional.of(mockToken));

      // When & Then
      assertThrows(CustomException.class, () -> {
        refreshTokenService.getUserByRefreshToken(tokenValue);
      });
    }
  }

  @Nested
  @DisplayName("Refresh Token 무효화 테스트")
  class InvalidateRefreshTokenTest {

    @Test
    @DisplayName("성공 - 단일 기기 토큰 무효화")
    void invalidateRefreshToken_success() {
      // Given
      Long userId = 1L;
      String deviceInfo = "Chrome";

      doNothing().when(redisRefreshTokenRepository).deleteByUserAndDevice(userId, deviceInfo);

      // When
      refreshTokenService.invalidateRefreshToken(userId, deviceInfo);

      // Then
      verify(redisRefreshTokenRepository, times(1)).deleteByUserAndDevice(userId, deviceInfo);
    }

    @Test
    @DisplayName("성공 - 모든 기기 토큰 무효화")
    void invalidateAllRefreshTokens_success() {
      // Given
      Long userId = 1L;

      doNothing().when(redisRefreshTokenRepository).deleteAllByUser(userId);

      // When
      refreshTokenService.invalidateAllRefreshTokens(userId);

      // Then
      verify(redisRefreshTokenRepository, times(1)).deleteAllByUser(userId);
    }
  }

  @Nested
  @DisplayName("Access Token 재발급 테스트")
  class RefreshAccessTokenTest {

    @Test
    @DisplayName("성공 - Access Token 재발급")
    void refreshAccessToken_success() {
      // Given
      String refreshTokenValue = "valid-refresh-token";

      RedisRefreshTokenDto mockToken = mock(RedisRefreshTokenDto.class);
      when(mockToken.getUserId()).thenReturn(1L);
      when(mockToken.isValid()).thenReturn(true);

      User mockUser = mock(User.class);
      when(mockUser.getId()).thenReturn(1L);

      when(redisRefreshTokenRepository.findByTokenValue(refreshTokenValue))
          .thenReturn(Optional.of(mockToken));
      when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
      when(accessTokenService.genAccessToken(mockUser)).thenReturn("new-access-token");

      // When
      String newAccessToken = refreshTokenService.refreshAccessToken(refreshTokenValue);

      // Then
      assertEquals("new-access-token", newAccessToken);
      verify(accessTokenService, times(1)).genAccessToken(mockUser);
    }
  }
}