package com.deliveranything.global.security.filter;

import com.deliveranything.domain.auth.service.AuthTokenService;
import com.deliveranything.domain.auth.service.UserAuthorityProvider;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.repository.UserRepository;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.global.security.auth.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;
  private final AuthTokenService authTokenService;
  private final UserAuthorityProvider userAuthorityProvider;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws IOException {

    log.debug("Processing request for {}", request.getRequestURI());

    try {
      processAuthentication(request, response, filterChain);
    } catch (CustomException e) {
      handleAuthenticationError(response, e);
    } catch (Exception e) {
      log.error("Unexpected error during authentication processing", e);
      handleAuthenticationError(response, new CustomException(ErrorCode.USER_NOT_FOUND));
    }
  }

  private void processAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // API 요청이 아니면 패스
    if (!request.getRequestURI().startsWith("/api/")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 인증이 불필요한 엔드포인트는 패스
    if (isPublicEndpoint(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    // Access Token 추출
    String accessToken = extractAccessToken(request);

    log.debug("accessToken: {}", accessToken);

    // 토큰이 없으면 패스 (익명 사용자)
    if (!StringUtils.hasText(accessToken)) {
      filterChain.doFilter(request, response);
      return;
    }

    User user = null;
    boolean isAccessTokenValid = false;

    // Access Token으로 인증
    user = authenticateWithAccessToken(accessToken);
    if (user != null) {
      isAccessTokenValid = true;
    }

    if (user == null) {
      throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    // ========== Access Token 자동 재발급 ==========
    if (StringUtils.hasText(accessToken) && !isAccessTokenValid) {
      String newAccessToken = authTokenService.genAccessToken(user);
      setCookieAndHeader(response, "accessToken", newAccessToken);
      log.info("Access Token 자동 재발급: userId={}", user.getId());
    }

    // 온보딩 필수 엔드포인트 체크
    if (requiresOnboarding(request.getRequestURI()) && !user.isOnboardingCompleted()) {
      log.warn("온보딩 미완료 사용자의 보호된 엔드포인트 접근 시도: userId={}, uri={}",
          user.getId(), request.getRequestURI());
      throw new CustomException(ErrorCode.ONBOARDING_NOT_COMPLETED);
    }

    // SecurityContext에 인증 정보 설정
    setAuthentication(user);

    filterChain.doFilter(request, response);
  }

  /**
   * 온보딩 필수 엔드포인트 체크
   */
  private boolean requiresOnboarding(String uri) {
    // 온보딩 완료 전에는 접근 불가한 엔드포인트 목록
    List<String> protectedPaths = List.of(
        "/api/v1/users/me/profile/switch",  // 프로필 전환
        "/api/v1/stores",                    // 상점 관리
        "/api/v1/products",                  // 상품 관리
        "/api/v1/orders",                    // 주문 관리
        "/api/v1/deliveries",                // 배달 관리
        "/api/v1/reviews",                   // 리뷰 관리
        "/api/v1/payments",                  // 결제 관리
        "/api/v1/settlements"                // 정산 관리
    );

    // 온보딩 완료 전에도 접근 가능한 예외 엔드포인트
    List<String> allowedPaths = List.of(
        "/api/v1/users/me/onboarding",      // 온보딩 처리
        "/api/v1/users/me/profiles",         // 프로필 목록 조회 (온보딩 선택용)
        "/api/v1/users/me",                  // 내 정보 조회
        "/api/v1/auth/logout"                // 로그아웃
    );

    // 예외 엔드포인트는 온보딩 불필요
    for (String allowed : allowedPaths) {
      if (uri.startsWith(allowed)) {
        return false;
      }
    }

    // 보호된 엔드포인트는 온보딩 필수
    for (String protected_ : protectedPaths) {
      if (uri.startsWith(protected_)) {
        return true;
      }
    }

    return false;  // 기타 엔드포인트는 온보딩 불필요
  }

  /**
   * 공개 엔드포인트 체크
   */
  private boolean isPublicEndpoint(String uri) {
    List<String> publicPaths = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/signup",
        "/api/v1/auth/logout",
        "/api/v1/auth/refresh",
        "/api/v1/auth/verification/send",
        "/api/v1/auth/verification/verify"
    );

    return publicPaths.contains(uri);
  }

  /**
   * 요청에서 Access Token 추출 (Authorization 헤더)
   */
  private String extractAccessToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");

    if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }

    return null;
  }

  /**
   * Access Token으로 인증 (Profile ID 정보 포함)
   */
  private User authenticateWithAccessToken(String accessToken) {
    // ✅ 토큰 유효성 검증 추가
    if (!authTokenService.isValidToken(accessToken)) {
      throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    if (authTokenService.isTokenExpired(accessToken)) {
      throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    }

    Map<String, Object> payload = authTokenService.payload(accessToken);

    if (payload != null) {
      Long userId = (Long) payload.get("id");

      String profileStr = (String) payload.get("currentActiveProfile");
      ProfileType currentActiveProfileType = null;
      if (profileStr != null) {
        try {
          currentActiveProfileType = ProfileType.valueOf(profileStr);
        } catch (IllegalArgumentException e) {
          log.warn("Invalid ProfileType in JWT: {}", profileStr);
        }
      }

      Long currentActiveProfileId = (Long) payload.get("currentActiveProfileId");

      User user = userRepository.findById(userId).orElse(null);

      // JWT - DB간 불일치 체크
      if (user != null) {
        Long dbProfileId = user.getCurrentActiveProfileId();

        if (currentActiveProfileId != null && dbProfileId != null) {
          if (!currentActiveProfileId.equals(dbProfileId)) {
            log.warn("프로필 불일치: userId={}, JWT={}, DB={}",
                userId, currentActiveProfileId, dbProfileId);
            return null;
          }
        }
        return user;
      }
    }

    return null;
  }

  /**
   * SecurityContext에 인증 정보 설정
   */
  private void setAuthentication(User user) {
    Collection<? extends GrantedAuthority> authorities = userAuthorityProvider.getAuthorities(user);

    UserDetails securityUser = new SecurityUser(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        "",
        user.getCurrentActiveProfile(),
        authorities
    );

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        securityUser,
        null,
        securityUser.getAuthorities()
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /**
   * 쿠키 및 헤더 설정
   */
  private void setCookieAndHeader(HttpServletResponse response, String name, String value) {
    response.addHeader("Set-Cookie",
        String.format("%s=%s; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=31536000",
            name, value));

    if ("accessToken".equals(name)) {
      response.setHeader("Authorization", "Bearer " + value);
    }
  }

  /**
   * 인증 오류 처리
   */
  private void handleAuthenticationError(HttpServletResponse response, CustomException e)
      throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(e.getHttpStatus().value());

    ApiResponse<Void> apiResponse = ApiResponse.fail(e.getCode(), e.getMessage());
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }
}