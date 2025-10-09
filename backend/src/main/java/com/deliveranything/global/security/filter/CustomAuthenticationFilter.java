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
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // ========== A안: Access Token 자동 재발급 (현재 적용) ==========
    // Access Token이 만료되었으면 새로 발급
    if (StringUtils.hasText(accessToken) && !isAccessTokenValid) {
      String newAccessToken = authTokenService.genAccessToken(user);
      setCookieAndHeader(response, "accessToken", newAccessToken);
      log.info("Access Token 자동 재발급: userId={}", user.getId());
    }

    /* ========== B안: 명시적 /refresh 호출 방식 (일단 주석 처리) ==========
     *
     * 장점:
     * - 깔끔한 책임 분리 (Filter는 인증만, /refresh는 재발급만)
     * - 보안 강화 (Refresh Token 없이는 재발급 불가)
     * - REST API 표준에 부합
     *
     * 프론트엔드 구현 필요:
     * - Axios Interceptor로 401 응답 시 자동으로 /refresh 호출
     * - 새 Access Token 받아서 원래 요청 재시도
     *
     * 예시 코드:
     * axios.interceptors.response.use(
     *   (response) => response,
     *   async (error) => {
     *     if (error.response?.status === 401) {
     *       const newToken = await fetch('/api/v1/auth/refresh', {
     *         method: 'POST',
     *         body: JSON.stringify({ refreshToken: getCookie('refreshToken') })
     *       });
     *       error.config.headers['Authorization'] = `Bearer ${newToken}`;
     *       return axios.request(error.config);
     *     }
     *   }
     * );
     *
     * B안 적용 시:
     * 1. 위의 A안 코드 블록 제거
     * 2. 이 주석 블록 제거
     * 3. /refresh 엔드포인트는 이미 구현되어 있음 (AuthController.java)
     */

    // SecurityContext에 인증 정보 설정
    setAuthentication(user);

    filterChain.doFilter(request, response);
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

    // Authorization 헤더에서 추출
    if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }

    // Fallback 제거: 헤더에 없으면 null 반환
    return null;
  }


  /**
   * Access Token으로 인증 (Profile ID 정보 포함)
   */
  private User authenticateWithAccessToken(String accessToken) {
    Map<String, Object> payload = authTokenService.payload(accessToken);

    if (payload != null) {
      Long userId = (Long) payload.get("id");

      // 안전한 타입 변환
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

      // JWT에서 사용자 정보 복원 (DB 조회 최소화)
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
        // Profile은 User.getCurrentActiveProfile() 호출 시 JPA가 자동으로 로드
        return user;
      }
    }

    return null;
  }

  /**
   * SecurityContext에 인증 정보 설정 -  UserAuthorityProvider 사용으로 변경 (Oauth2에 확장성 고려)
   */
  private void setAuthentication(User user) {
    //  Auth 도메인의 UserAuthorityProvider를 통해 권한 생성
    Collection<? extends GrantedAuthority> authorities = userAuthorityProvider.getAuthorities(user);

    UserDetails securityUser = new SecurityUser(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        "",  // 비밀번호는 빈 문자열
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
   * 쿠키 값 추출
   */
  private String getCookieValue(HttpServletRequest request, String name) {
    if (request.getCookies() != null) {
      for (var cookie : request.getCookies()) {
        if (name.equals(cookie.getName())) {
          return StringUtils.hasText(cookie.getValue()) ? cookie.getValue() : null;
        }
      }
    }
    return null;
  }

  /**
   * 쿠키 및 헤더 설정
   */
  private void setCookieAndHeader(HttpServletResponse response, String name, String value) {
    // 쿠키 설정
    response.addHeader("Set-Cookie",
        String.format("%s=%s; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=31536000",
            name, value));

    // Authorization 헤더 설정
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
