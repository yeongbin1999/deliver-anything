package com.deliveranything.domain.auth.repository;

import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  // 활성화된 토큰 값으로 조회
  Optional<RefreshToken> findByTokenValueAndIsActiveTrue(String tokenValue);

  // 사용자의 모든 활성화된 토큰 조회
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.isActive = true")
  List<RefreshToken> findActiveTokensByUser(@Param("user") User user);

  // 사용자의 특정 디바이스 토큰 조회
  Optional<RefreshToken> findByUserAndDeviceInfoAndIsActiveTrue(User user, String deviceInfo);

  // 사용자의 모든 토큰 비활성화
  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.user = :user")
  void deactivateAllTokensByUser(@Param("user") User user);

  // 특정 사용자의 특정 디바이스 토큰만 비활성화
  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.user = :user AND rt.deviceInfo = :deviceInfo")
  void deactivateTokenByUserAndDevice(@Param("user") User user,
      @Param("deviceInfo") String deviceInfo);

  // 만료된 토큰들 정리 (배치 작업용)
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.isActive = false")
  void deleteExpiredAndInactiveTokens(@Param("now") LocalDateTime now);


  // 사용자별 활성 토큰 개수 조회
  @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.isActive = true")
  long countActiveTokensByUser(@Param("user") User user);

  // 유효한 토큰인지 확인 (활성화 상태이고 만료되지 않음)
  @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenValue = :tokenValue AND rt.isActive = true AND rt.expiresAt > :now")
  Optional<RefreshToken> findValidTokenByValue(@Param("tokenValue") String tokenValue,
      @Param("now") LocalDateTime now);
}