package com.deliveranything.domain.user.entity;

import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.enums.SocialProvider;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column(name = "email", unique = true, nullable = false, columnDefinition = "VARCHAR(100)")
  private String email;

  @Column(name = "password", columnDefinition = "VARCHAR(255)")
  private String password;

  @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(50)")
  private String name;

  @Column(name = "phone_number", unique = true, columnDefinition = "VARCHAR(20)")
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "social_provider", nullable = false, columnDefinition = "VARCHAR(20)")
  private SocialProvider socialProvider;

  @Column(name = "social_id", columnDefinition = "VARCHAR(100)")
  private String socialId;

  @Column(name = "api_key", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
  private String apiKey;

  // 현재 활성화된 프로필 (이제 Profile 테이블의 전역 고유 ID)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "current_active_profile_id")
  private Profile currentActiveProfile;

  @Column(name = "is_onboarding_completed", nullable = false)
  private boolean isOnboardingCompleted;

  @Column(name = "is_email_verified", nullable = false)
  private boolean isEmailVerified;

  @Column(name = "is_enabled", nullable = false)
  private boolean isEnabled;

  @Column(name = "is_admin", nullable = false)
  private boolean isAdmin;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  // Profile과의 관계
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Profile> profiles = new ArrayList<>();

  @Builder
  public User(String email, String password, String name, String phoneNumber,
      SocialProvider socialProvider, String socialId, Profile currentActiveProfile) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.socialProvider = socialProvider != null ? socialProvider : SocialProvider.LOCAL;
    this.socialId = socialId;
    this.apiKey = generateApiKey();
    this.currentActiveProfile = currentActiveProfile; // 현재 프로필 아이디
    this.isOnboardingCompleted = false;
    this.isEmailVerified = false;
    this.isEnabled = true;
    this.isAdmin = false;
  }

  // ========== 비즈니스 메서드 ==========

  /**
   * 온보딩 완료 처리
   */
  public void completeOnboarding(Profile selectedProfile) {
    this.currentActiveProfile = selectedProfile;
    this.isOnboardingCompleted = true;
  }

  /**
   * 프로필 전환
   */
  public void switchProfile(Profile targetProfile) {
    // 해당 사용자의 프로필인지 확인
    if (!targetProfile.belongsToUser(this.getId())) {
      throw new IllegalStateException("해당 프로필은 현재 사용자의 프로필이 아닙니다.");
    }

    // 활성 프로필인지 확인
    if (!targetProfile.isActive()) {
      throw new IllegalStateException("비활성화된 프로필로는 전환할 수 없습니다.");
    }

    this.currentActiveProfile = targetProfile;
  }

  /**
   * 특정 타입의 프로필을 가지고 있는지 확인
   */
  public boolean hasProfileType(ProfileType profileType) {
    return profiles.stream()
        .anyMatch(profile -> profile.getType() == profileType && profile.isActive());
  }

  /**
   * 고객 프로필 보유 여부
   */
  public boolean hasCustomerProfile() {
    return hasProfileType(ProfileType.CUSTOMER);
  }

  /**
   * 판매자 프로필 보유 여부
   */
  public boolean hasSellerProfile() {
    return hasProfileType(ProfileType.SELLER);
  }

  /**
   * 라이더 프로필 보유 여부
   */
  public boolean hasRiderProfile() {
    return hasProfileType(ProfileType.RIDER);
  }

  /**
   * 현재 활성 프로필 타입 조회
   */
  public ProfileType getCurrentActiveProfileType() {
    return currentActiveProfile != null ? currentActiveProfile.getType() : null;
  }

  /**
   * 현재 활성 프로필 ID 조회 (전역 고유 ID)
   */
  public Long getCurrentActiveProfileId() {
    return currentActiveProfile != null ? currentActiveProfile.getId() : null;
  }

  /**
   * 활성 프로필 목록 조회
   */
  public List<ProfileType> getActiveProfileTypes() {
    return profiles.stream()
        .filter(Profile::isActive)
        .map(Profile::getType)
        .toList();
  }

  /**
   * 특정 타입의 프로필 조회
   */
  public Profile getProfileByType(ProfileType profileType) {
    return profiles.stream()
        .filter(profile -> profile.getType() == profileType && profile.isActive())
        .findFirst()
        .orElse(null);
  }

  // ========== 인증/인가 관련 ==========

  public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void verifyEmail() {
    this.isEmailVerified = true;
  }

  public void updateLastLoginAt() {
    this.lastLoginAt = LocalDateTime.now();
  }

  private String generateApiKey() {
    return UUID.randomUUID().toString();
  }


  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // 기본 사용자 권한
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    // currentActiveProfile 기반 권한 추가
    if (currentActiveProfile != null) {
      ProfileType currentType = currentActiveProfile.getType();
      authorities.add(new SimpleGrantedAuthority("ROLE_" + currentType.name()));

      /***
       * 활성화된 다른 프로필들의 권한도 추가 (멀티프로필 지원) - 현재 쓸모가 없어보여서 주석처리
       for (Profile profile : profiles) {
       if (profile.isActive()) {
       authorities.add(new SimpleGrantedAuthority("PROFILE_" + profile.getType().name()));
       }
       }
       ***/
    }

    // 관리자 권한
    if (isAdmin) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    return authorities;
  }
  
  public boolean isAccountNonExpired() {
    return true;
  }


  public boolean isAccountNonLocked() {
    return true;
  }


  public boolean isCredentialsNonExpired() {
    return true;
  }

}