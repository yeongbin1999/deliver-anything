package com.deliveranything.domain.user.entity;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.entity.profile.SellerProfile;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {

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

  // Spring Security UserDetails 구현을 위한 필드들
  @Column(name = "account_non_expired", nullable = false)
  private boolean accountNonExpired;

  @Column(name = "account_non_locked", nullable = false)
  private boolean accountNonLocked;

  @Column(name = "credentials_non_expired", nullable = false)
  private boolean credentialsNonExpired;

  // 프로필 관리
  @Enumerated(EnumType.STRING)
  @Column(name = "current_active_profile")
  private ProfileType currentActiveProfile;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted;

  @Column(name = "current_active_profile_id") // 페이로드에 포함시킬 예정
  private Long currentActiveProfileId;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "api_key", unique = true)
  private String apiKey;

  // 연관관계 매핑
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private CustomerProfile customerProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private SellerProfile sellerProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private RiderProfile riderProfile;

  @Builder
  public User(String email, String password, String name, String phoneNumber,
      SocialProvider socialProvider, String socialId) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.socialProvider = socialProvider != null ? socialProvider : SocialProvider.LOCAL;
    this.socialId = socialId;

    // 계정 상태 초기값 설정
    this.isEmailVerified = false;
    this.isEnabled = true;
    this.accountNonExpired = true;
    this.accountNonLocked = true;
    this.credentialsNonExpired = true;
    this.onboardingCompleted = false;

    // apiKey 자동 생성
    this.apiKey = UUID.randomUUID().toString();
  }

  // Spring Security - UserDetails 구현

  @Override // UserDetails 인터페이스의 메서드 오버라이딩
  public String getUsername() {
    return email; // 이메일을 username으로 사용
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // 관리자 권한 (항상 유지)
    if (isAdmin()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    // 현재 활성화된 프로필의 권한만 부여
    if (currentActiveProfile != null) {
      switch (currentActiveProfile) {
        case CUSTOMER:
          if (hasCustomerProfile()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
          }
          break;
        case SELLER:
          if (hasSellerProfile()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
          }
          break;
        case RIDER:
          if (hasRiderProfile()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_RIDER"));
          }
          break;
      }
    }

    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  // 비즈니스 메서드

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
    this.currentActiveProfileId = getProfileIdByType(selectedProfile);
    this.onboardingCompleted = true;
  }

  public void switchProfile(ProfileType targetProfile) {
    if (!hasProfileForType(targetProfile)) {
      throw new IllegalStateException(targetProfile + " 프로필이 존재하지 않습니다");
    }
    this.currentActiveProfile = targetProfile;
    this.currentActiveProfileId = getProfileIdByType(targetProfile);
  }

  private boolean hasProfileForType(ProfileType profileType) {
    return switch (profileType) {
      case CUSTOMER -> hasCustomerProfile();
      case SELLER -> hasSellerProfile();
      case RIDER -> hasRiderProfile();
    };
  }

  private Long getProfileIdByType(ProfileType profileType) {
    return switch (profileType) {
      case CUSTOMER -> customerProfile != null ? customerProfile.getId() : null;
      case SELLER -> sellerProfile != null ? sellerProfile.getId() : null;
      case RIDER -> riderProfile != null ? riderProfile.getId() : null;
    };
  }

  public void enable() {
    this.isEnabled = true;
  }

  public void disable() {
    this.isEnabled = false;
  }

  public boolean hasCustomerProfile() {
    return customerProfile != null;
  }

  public boolean hasSellerProfile() {
    return sellerProfile != null;
  }

  public boolean hasRiderProfile() {
    return riderProfile != null;
  }

  // 사용 가능한 프로필 목록 반환 (JWT 페이로드용)
  public List<ProfileType> getAvailableProfiles() {
    List<ProfileType> profiles = new ArrayList<>();
    if (hasCustomerProfile()) {
      profiles.add(ProfileType.CUSTOMER);
    }
    if (hasSellerProfile()) {
      profiles.add(ProfileType.SELLER);
    }
    if (hasRiderProfile()) {
      profiles.add(ProfileType.RIDER);
    }
    return profiles;
  }

  // 관리자 권한 확인
  public boolean isAdmin() {
    // 추후 관리자 테이블 또는 권한 시스템으로 확장 예정
    return "admin@deliveranything.com".equals(email) ||
        "system@deliveranything.com".equals(email);
  }

  public void modifyApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  // 테스트용 임시 세터 메서드
  public void setCustomerProfile(CustomerProfile customerProfile) {
    this.customerProfile = customerProfile;
  }

  public void setSellerProfile(SellerProfile sellerProfile) {
    this.sellerProfile = sellerProfile;
  }

  public void setRiderProfile(RiderProfile riderProfile) {
    this.riderProfile = riderProfile;
  }
}