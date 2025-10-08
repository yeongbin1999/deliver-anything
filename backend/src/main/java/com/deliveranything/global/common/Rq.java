package com.deliveranything.global.common;

import com.deliveranything.domain.user.profile.entity.Profile;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.service.UserService;
import com.deliveranything.global.security.auth.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Request/Response 처리 헬퍼 클래스 멀티 프로필 시스템에 맞게 확장 Profile 기반 전역 고유 ID 지원
 */
@Component
@RequiredArgsConstructor
public class Rq {

  private final HttpServletRequest req;
  private final HttpServletResponse resp;
  private final UserService userService;

  /**
   * 현재 인증된 사용자 조회 (멀티 프로필 정보 포함)
   */
  public User getActor() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .filter(principal -> principal instanceof SecurityUser)
        .map(principal -> (SecurityUser) principal)
        .map(securityUser -> {
          // SecurityUser에서 User 객체 생성 (프로필 정보 포함)
          User user = User.builder()
              .email(securityUser.getEmail())
              .password(null)
              .username(securityUser.getUserName())
              .phoneNumber(null)
              .socialProvider(null)
              .socialId(null)
              .currentActiveProfile(
                  securityUser.getCurrentActiveProfile()) // Profile 엔티티는 여기서 설정하지 않음
              .build();
          return user;
        })
        .orElse(null);
  }

  public User getActorFromDb() {
    User actor = getActor();

    if (actor == null) {
      return null;
    }

    return userService.findById(actor.getId());
  }

  /**
   * 현재 활성화된 프로필 타입 조회
   */
  public Profile getCurrentProfile() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .filter(principal -> principal instanceof SecurityUser)
        .map(principal -> (SecurityUser) principal)
        .map(SecurityUser::getCurrentActiveProfile)
        .orElse(null);
  }

  /**
   * 현재 활성화된 프로필 ID 조회 (전역 고유 Profile ID)
   */
  public Long getCurrentProfileId() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .filter(principal -> principal instanceof SecurityUser)
        .map(principal -> (SecurityUser) principal)
        .map(SecurityUser::getCurrentActiveProfile).get().getId();
  }

  /**
   * 특정 프로필이 활성화되어 있는지 확인
   */
  public boolean hasActiveProfile(ProfileType profileType) {
    ProfileType currentProfile = getCurrentProfile().getType();
    return currentProfile == profileType;
  }

  /**
   * 소비자 프로필이 활성화되어 있는지 확인
   */
  public boolean isCustomerActive() {
    return hasActiveProfile(ProfileType.CUSTOMER);
  }

  /**
   * 판매자 프로필이 활성화되어 있는지 확인
   */
  public boolean isSellerActive() {
    return hasActiveProfile(ProfileType.SELLER);
  }

  /**
   * 배달원 프로필이 활성화되어 있는지 확인
   */
  public boolean isRiderActive() {
    return hasActiveProfile(ProfileType.RIDER);
  }

  /**
   * 사용자가 인증되어 있는지 확인
   */
  public boolean isAuthenticated() {
    return getActor() != null;
  }

  /**
   * 관리자인지 확인
   */
  public boolean isAdmin() {
    User actor = getActor();
    return actor != null && actor.isAdmin();
  }

  // ========== HTTP 헤더 관리 ==========

  /**
   * 요청 헤더 값 조회
   */
  public String getHeader(String name, String defaultValue) {
    return Optional.ofNullable(req.getHeader(name))
        .filter(headerValue -> !headerValue.isBlank())
        .orElse(defaultValue);
  }

  /**
   * 응답 헤더 설정
   */
  public void setHeader(String name, String value) {
    if (value == null) {
      value = "";
    }

    if (value.isBlank()) {
      // 빈 값이면 헤더 제거
      return;
    } else {
      resp.setHeader(name, value);
    }
  }

  // ========== 쿠키 관리 ==========

  /**
   * 쿠키 값 조회
   */
  public String getCookieValue(String name, String defaultValue) {
    return Optional.ofNullable(req.getCookies())
        .flatMap(cookies ->
            Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst()
        )
        .orElse(defaultValue);
  }

  /**
   * 쿠키 설정
   */
  public void setCookie(String name, String value) {
    if (value == null) {
      value = "";
    }

    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setDomain("localhost");
    cookie.setSecure(true);
    cookie.setAttribute("SameSite", "Strict");

    if (value.isBlank()) {
      cookie.setMaxAge(0); // 쿠키 삭제
    } else {
      cookie.setMaxAge(60 * 60 * 24 * 365); // 1년
    }

    resp.addCookie(cookie);
  }

  /**
   * 쿠키 삭제
   */
  public void deleteCookie(String name) {
    setCookie(name, "");
  }

  // ========== JWT 토큰 관리 ==========

  /**
   * Authorization 헤더에서 토큰 추출
   */
  public String getAccessTokenFromHeader() {
    String authorization = getHeader("Authorization", "");
    if (authorization.startsWith("Bearer ")) {
      String[] tokens = authorization.substring(7).split(" ", 2);
      return tokens.length > 1 ? tokens[1] : null; // accessToken 부분
    }
    return null;
  }

  /**
   * Authorization 헤더에서 apiKey 추출
   */
  public String getApiKeyFromHeader() {
    String authorization = getHeader("Authorization", "");
    if (authorization.startsWith("Bearer ")) {
      String[] tokens = authorization.substring(7).split(" ", 2);
      return tokens.length > 0 ? tokens[0] : null; // apiKey 부분
    }
    return null;
  }

  /**
   * 새로운 Access Token을 Authorization 헤더와 쿠키에 설정
   */
  public void setAccessToken(String accessToken) {
    setHeader("Authorization", "Bearer " + accessToken);
    setCookie("accessToken", accessToken);
  }

  /**
   * API Key를 쿠키에 설정
   */
  public void setApiKey(String apiKey) {
    setCookie("apiKey", apiKey);
  }

  // ========== 프로필 전환 지원 ==========

  /**
   * 프로필 전환 요청 시 사용할 헤더 정보
   */
  public ProfileType getRequestedProfileFromHeader() {
    String profileHeader = getHeader("X-Active-Profile", null);
    if (profileHeader != null) {
      try {
        return ProfileType.valueOf(profileHeader.toUpperCase());
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * 현재 프로필 정보를 응답 헤더에 설정
   */
  public void setCurrentProfileHeader(ProfileType profileType) {
    if (profileType != null) {
      setHeader("X-Current-Profile", profileType.name());
    }
  }

  /**
   * 현재 프로필 ID를 응답 헤더에 설정 (전역 고유 Profile ID)
   */
  public void setCurrentProfileIdHeader(Long profileId) {
    if (profileId != null) {
      setHeader("X-Current-Profile-Id", profileId.toString());
    }
  }

  /**
   * 지정된 URL로 리다이렉트
   */
  @SneakyThrows
  public void sendRedirect(String url) {
    resp.sendRedirect(url);
  }

  /**
   * SSE 연결용 전역 고유 키 생성
   */
  public String generateGlobalProfileKey() {
    User actor = getActor();
    ProfileType currentProfile = getCurrentProfile().getType();
    Long currentProfileId = getCurrentProfile().getId();

    if (actor != null && currentProfile != null && currentProfileId != null) {
      return String.format("%d_%s_%d", actor.getId(), currentProfile.name(), currentProfileId);
    }
    return null;
  }
}