package com.deliveranything.global.security;

import com.deliveranything.domain.user.enums.ProfileType;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

// Spring Security 인증 객체 멀티 프로필 고려
@Getter
public class SecurityUser extends User {

  private final Long id;
  private final String name;
  private final ProfileType currentActiveProfile;
  private final Long currentActiveProfileId;

  public SecurityUser(
      Long id,
      String username,
      String password,
      String name,
      ProfileType currentActiveProfile,
      Long currentActiveProfileId,
      Collection<? extends GrantedAuthority> authorities
  ) {
    super(username, password, authorities);
    this.id = id;
    this.name = name;
    this.currentActiveProfile = currentActiveProfile;
    this.currentActiveProfileId = currentActiveProfileId;
  }

  // 현재 활성화된 프로필이 특정 타입인지 확인
  public boolean hasActiveProfile(ProfileType profileType) {
    return currentActiveProfile == profileType;
  }

  // 현재 활성화된 프로필 ID 반환 (null 안전)
  public Long getCurrentActiveProfileIdSafe() {
    return currentActiveProfileId != null ? currentActiveProfileId : 0L;
  }
}