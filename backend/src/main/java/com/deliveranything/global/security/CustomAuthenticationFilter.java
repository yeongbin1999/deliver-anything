package com.deliveranything.global.security;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.Profile;
import com.deliveranything.domain.user.entity.token.RefreshToken;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.repository.ProfileRepository;
import com.deliveranything.domain.user.repository.RefreshTokenRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.domain.user.service.AuthTokenService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import com.deliveranything.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
  private final ProfileRepository profileRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthTokenService authTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

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

    // 토큰 추출
    String[] tokens = extractTokens(request);
    String apiKeyOrRefreshToken = tokens[0];
    String accessToken = tokens[1];

    log.debug("apiKeyOrRefreshToken: {}", apiKeyOrRefreshToken);
    log.debug("accessToken: {}", accessToken);

    // 토큰이 없으면 패스 (익명 사용자)
    if (!StringUtils.hasText(apiKeyOrRefreshToken) && !StringUtils.hasText(accessToken)) {
      filterChain.doFilter(request, response);
      return;
    }

    User user = null;
    boolean isAccessTokenValid = false;

    // 1순위: Access Token 검증
    if (StringUtils.hasText(accessToken)) {
      user = authenticateWithAccessToken(accessToken);
      if (user != null) {
        isAccessTokenValid = true;
      }
    }

    // 2순위: RefreshToken 또는 apiKey로 인증
    if (user == null && StringUtils.hasText(apiKeyOrRefreshToken)) {
      user = authenticateWithRefreshTokenOrApiKey(apiKeyOrRefreshToken);
    }

    if (user == null) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // Access Token이 만료되었으면 새로 발급
    if (StringUtils.hasText(accessToken) && !isAccessTokenValid) {
      String newAccessToken = authTokenService.genAccessToken(user);
      setCookieAndHeader(response, "accessToken", newAccessToken);
    }

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
        "/api/v1/auth/register",
        "/api/v1/auth/logout",
        "/api/v1/auth/verification/send",
        "/api/v1/auth/verification/verify"
    );

    return publicPaths.contains(uri);
  }

  /**
   * 요청에서 토큰 추출 (Authorization 헤더 또는 쿠키)
   */
  private String[] extractTokens(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");

    if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
      // Authorization: Bearer {apiKey} {accessToken} 형태
      String[] tokens = authorization.substring(7).split(" ", 2);
      return new String[]{
          tokens.length > 0 ? tokens[0] : null,
          tokens.length > 1 ? tokens[1] : null
      };
    }

    // 쿠키에서 추출
    String apiKey = getCookieValue(request, "apiKey");
    String accessToken = getCookieValue(request, "accessToken");

    return new String[]{apiKey, accessToken};
  }

  /**
   * Access Token으로 인증 (Profile ID 정보 포함)
   */
  private User authenticateWithAccessToken(String accessToken) {
    Map<String, Object> payload = authTokenService.payload(accessToken);

    if (payload != null) {
      Long userId = (Long) payload.get("id");
      String name = (String) payload.get("name");

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
      if (user != null) {
        // Profile 정보를 DB에서 조회해서 User에 설정
        if (currentActiveProfileId != null && currentActiveProfileId > 0) {
          Profile activeProfile = profileRepository.findById(currentActiveProfileId).orElse(null);
          if (activeProfile != null) {
            // User 엔티티의 currentActiveProfile 필드에 설정
            // 실제로는 User의 setter가 필요하거나 생성자를 통해 설정해야 함
          }
        }
        return user;
      }
    }

    return null;
  }

  /**
   * RefreshToken 또는 apiKey로 인증
   */
  private User authenticateWithRefreshTokenOrApiKey(String token) {
    // 1순위: RefreshToken 테이블에서 확인
    RefreshToken refreshToken = refreshTokenRepository
        .findByTokenValueAndIsActiveTrue(token)
        .orElse(null);

    if (refreshToken != null && refreshToken.isValid()) {
      return refreshToken.getUser();
    }

    // 2순위: apiKey로 확인 (강사님 방식 호환)
    return userRepository.findByApiKey(token).orElse(null);
  }

  /**
   * SecurityContext에 인증 정보 설정 (Profile ID 포함)
   */
  private void setAuthentication(User user) {
    UserDetails securityUser = new SecurityUser(
        user.getId(),
        user.getUsername(),
        "",  // 비밀번호는 빈 문자열
        user.getName(),
        user.getCurrentActiveProfileType(), // 현재 활성 프로필 타입
        user.getCurrentActiveProfileId(),   // 전역 고유 Profile ID
        user.getAuthorities()
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
    response.getWriter().write(Ut.json.toString(apiResponse));
  }
}