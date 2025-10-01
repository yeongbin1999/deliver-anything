package com.deliveranything.global.security.auth;

import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.enums.ProfileType;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class SecurityUser extends User {

  private final Long id;
  private final String name;
  private final Profile currentActiveProfile;  // 이제 전역 고유 Profile ID

  public SecurityUser(
      Long id,
      String name,
      String password,
      Profile currentActiveProfile,
      Collection<? extends GrantedAuthority> authorities
  ) {
    super(name, password, authorities);
    this.id = id;
    this.name = name;
    this.currentActiveProfile = currentActiveProfile;
  }

  // 현재 활성화된 프로필이 특정 타입인지 확인
  public boolean hasActiveProfile(ProfileType profileType) {
    return currentActiveProfile.getType() == profileType;
  }

  // 현재 활성화된 프로필 ID 반환 (null 안전)
  public Long getCurrentActiveProfileIdSafe() {
    return currentActiveProfile != null ? currentActiveProfile.getId() : 0L;
  }

  // 고객 프로필 활성화 여부
  public boolean isCustomerActive() {
    return hasActiveProfile(ProfileType.CUSTOMER);
  }

  // 판매자 프로필 활성화 여부
  public boolean isSellerActive() {
    return hasActiveProfile(ProfileType.SELLER);
  }

  // 라이더 프로필 활성화 여부
  public boolean isRiderActive() {
    return hasActiveProfile(ProfileType.RIDER);
  }
}