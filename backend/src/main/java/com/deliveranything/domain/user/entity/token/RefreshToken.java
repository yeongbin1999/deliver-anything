package com.deliveranything.domain.user.entity.token;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token_value", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
  private String tokenValue;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "device_info", columnDefinition = "VARCHAR(255)")
  private String deviceInfo; // User-Agent, 디바이스 식별 정보 등 ... 우선 구현

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Builder
  public RefreshToken(User user, LocalDateTime expiresAt, String deviceInfo) {
    this.user = user;
    this.tokenValue = UUID.randomUUID().toString();
    this.expiresAt = expiresAt;
    this.deviceInfo = deviceInfo;
    this.isActive = true;
  }

  // 비즈니스 메서드

  // 토큰이 만료되었는지 확인
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  // 토큰이 유효한지 확인 (활성화 상태이고 만료되지 않음)

  public boolean isValid() {
    return isActive && !isExpired();
  }

  // 토큰 비활성화 (로그아웃, 보안상 무효화)

  public void deactivate() {
    this.isActive = false;
  }

  // 토큰 만료 시간 연장
  public void extendExpiration(LocalDateTime newExpiresAt) {
    if (newExpiresAt.isAfter(LocalDateTime.now())) {
      this.expiresAt = newExpiresAt;
    }
  }

  //새로운 토큰 값으로 갱신 (보안상 토큰 rotation)

  public void rotateToken() {
    this.tokenValue = UUID.randomUUID().toString();
  }

  // 특정 사용자의 토큰인지 확인

  public boolean belongsToUser(Long userId) {
    return user != null && user.getId().equals(userId);
  }
}
