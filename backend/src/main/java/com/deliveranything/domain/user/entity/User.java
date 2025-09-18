package com.deliveranything.domain.user.entity;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.enums.SocialProvider;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  // 기본 사용자 정보
  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "phone_number", unique = true, nullable = false, length = 20)
  private String phoneNumber;

  // 소셜 로그인 정보
  @Enumerated(EnumType.STRING)
  @Column(name = "social_provider", nullable = false)
  private SocialProvider socialProvider;

  @Column(name = "social_id")
  private String socialId;

  // 계정 상태
  @Column(name = "is_email_verified", nullable = false)
  private boolean isEmailVerified;

  @Column(name = "is_enabled", nullable = false)
  private boolean isEnabled;

  // 프로필 관리
  @Enumerated(EnumType.STRING)
  @Column(name = "current_active_profile")
  private ProfileType currentActiveProfile;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted;

  // 기타
  @Column(name = "default_address_id")
  private Long defaultAddressId;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  // 연관관계 매핑
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private CustomerProfile customerProfile;

  @Builder
  public User(String email, String password, String name, String phoneNumber,
      SocialProvider socialProvider, String socialId) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.socialProvider = socialProvider != null ? socialProvider : SocialProvider.LOCAL;
    this.socialId = socialId;
    this.isEmailVerified = false;
    this.isEnabled = true;
    this.onboardingCompleted = false;
  }

  public void setDefaultAddress(Long addressId) {
    this.defaultAddressId = addressId;
  }

  public void updateBasicInfo(String name, String phoneNumber) {
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }

  public void verifyEmail() {
    this.isEmailVerified = true;
  }

  public void updateLastLoginAt() {
    this.lastLoginAt = LocalDateTime.now();
  }

  public void completeOnboarding(ProfileType selectedProfile) {
    this.currentActiveProfile = selectedProfile;
    this.onboardingCompleted = true;
  }

  public void switchProfile(ProfileType targetProfile) {
    this.currentActiveProfile = targetProfile;
  }
}