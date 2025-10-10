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

    String uri = request.getRequestURI();
    String method = request.getMethod();

    // ✅ 무조건 찍히는 로그 (System.out + log.error 둘 다)
    System.out.println("🔍🔍🔍 FILTER 진입!!!! URI: " + uri + ", Method: " + method);

    try {
      processAuthentication(request, response, filterChain);
      System.out.println("✅✅✅ FILTER 정상 통과: " + uri);
    } catch (CustomException e) {
      System.out.println("❌❌❌ CustomException 발생: " + uri + ", " + e.getMessage());
      System.out.println("스택트레이스:");
      e.printStackTrace(System.out);  // ✅ System.out으로 스택트레이스 출력
      log.error("❌ CustomException 발생", e);
      handleAuthenticationError(response, e);
    } catch (ServletException e) {  // ✅ ServletException 추가
      System.out.println("❌❌❌ ServletException 발생: " + uri + ", " + e.getMessage());
      e.printStackTrace(System.out);
      log.error("❌ ServletException 발생", e);
      handleAuthenticationError(response, new CustomException(ErrorCode.TOKEN_INVALID));
    } catch (Exception e) {
      System.out.println("❌❌❌ Exception 발생: " + uri + ", " + e.getMessage());
      e.printStackTrace(System.out);  // ✅ System.out으로 스택트레이스 출력
      log.error("❌ Exception 발생", e);
      handleAuthenticationError(response, new CustomException(ErrorCode.USER_NOT_FOUND));
    }
  }

  private void processAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String uri = request.getRequestURI();
    System.out.println("✅ processAuthentication 진입: " + uri);

    // API 요청이 아니면 패스
    if (!uri.startsWith("/api/")) {
      System.out.println("⏭️ API 요청 아님, 패스");
      filterChain.doFilter(request, response);
      return;
    }

    // 인증이 불필요한 엔드포인트는 패스
    if (isPublicEndpoint(uri)) {
      System.out.println("⏭️ Public 엔드포인트, 패스");
      filterChain.doFilter(request, response);
      return;
    }

    // Access Token 추출
    String accessToken = extractAccessToken(request);
    System.out.println("🔑 Access Token: " + (accessToken != null ? "존재함" : "없음"));

    // 토큰이 없으면 패스 (익명 사용자)
    if (!StringUtils.hasText(accessToken)) {
      System.out.println("⏭️ 토큰 없음, 패스");
      filterChain.doFilter(request, response);
      return;
    }

    User user = null;
    boolean isAccessTokenValid = false;

    // Access Token으로 인증
    System.out.println("🔐 Access Token 검증 시작");
    user = authenticateWithAccessToken(accessToken);
    if (user != null) {
      isAccessTokenValid = true;
      System.out.println("✅ Access Token 유효, userId: " + user.getId());
    } else {
      System.out.println("❌ Access Token 무효");
    }

    if (user == null) {
      System.out.println("❌ 인증 실패, 예외 발생 예정");
      throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    // 온보딩 필수 엔드포인트 체크
    if (requiresOnboarding(uri) && !user.isOnboardingCompleted()) {
      System.out.println("❌ 온보딩 미완료");
      throw new CustomException(ErrorCode.ONBOARDING_NOT_COMPLETED);
    }

    // SecurityContext에 인증 정보 설정
    System.out.println("🔐 SecurityContext 설정 시작");
    setAuthentication(user);
    System.out.println("✅ SecurityContext 설정 완료");

    System.out.println("✅ 필터 통과, 다음 필터로 이동");
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
    System.out.println(
        " 토큰 검증 시작: " + accessToken.substring(0, Math.min(30, accessToken.length())) + "...");

    // ✅ 토큰 유효성 검증
    boolean isValid = authTokenService.isValidToken(accessToken);
    System.out.println(" isValidToken() 결과: " + isValid);

    if (!isValid) {
      System.out.println("❌ 토큰 검증 실패!");
      throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    boolean isExpired = authTokenService.isTokenExpired(accessToken);
    System.out.println("🔐 isTokenExpired() 결과: " + isExpired);

    if (isExpired) {
      System.out.println("❌ 토큰 만료!");
      throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    }

    System.out.println("✅ 토큰 검증 통과, payload 파싱 시작");
    Map<String, Object> payload = authTokenService.payload(accessToken);
    System.out.println(" Payload: " + payload);

    if (payload != null) {
      Long userId = (Long) payload.get("id");

      // ✅ String으로 가져오기
      String profileStr = (String) payload.get("currentActiveProfile");
      ProfileType currentActiveProfileType = null;
      if (profileStr != null && !profileStr.isEmpty()) {  // ✅ 빈 문자열 체크 추가
        try {
          currentActiveProfileType = ProfileType.valueOf(profileStr);
        } catch (IllegalArgumentException e) {
          log.warn("Invalid ProfileType in JWT: {}", profileStr);
        }
      }

      Long currentActiveProfileId = (Long) payload.get("currentActiveProfileId");

      User user = userRepository.findByIdWithProfile(userId).orElse(null);

      // JWT - DB간 불일치 체크 (온보딩 완료된 경우만)
      if (user != null && user.isOnboardingCompleted()) {  // ✅ 온보딩 완료 체크 추가
        Long dbProfileId = user.getCurrentActiveProfileId();

        if (currentActiveProfileId != null && dbProfileId != null) {
          if (!currentActiveProfileId.equals(dbProfileId)) {
            log.warn("프로필 불일치: userId={}, JWT={}, DB={}",
                userId, currentActiveProfileId, dbProfileId);
            return null;
          }
        }
      }
      return user;
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
        "",
        user.getEmail(),
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