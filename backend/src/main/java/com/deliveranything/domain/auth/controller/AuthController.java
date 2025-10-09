package com.deliveranything.domain.auth.controller;

import com.deliveranything.domain.auth.dto.LoginRequest;
import com.deliveranything.domain.auth.dto.LoginResponse;
import com.deliveranything.domain.auth.dto.RefreshTokenRequest;
import com.deliveranything.domain.auth.dto.RefreshTokenResponse;
import com.deliveranything.domain.auth.dto.SignupRequest;
import com.deliveranything.domain.auth.dto.SignupResponse;
import com.deliveranything.domain.auth.service.AuthService;
import com.deliveranything.domain.auth.service.TokenService;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.service.ProfileService;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.Rq;
import com.deliveranything.global.security.auth.SecurityUser;
import com.deliveranything.global.util.UserAgentUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증/인가 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final TokenService tokenService;
  private final ProfileService profileService;
  private final UserAgentUtil userAgentUtil;
  private final Rq rq;

  @PostMapping("/signup")
  @Operation(
      summary = "회원가입",
      description = "이메일과 비밀번호로 신규 회원가입을 진행합니다. 비밀번호는 8자 이상, 영문/숫자/특수문자 포함이어야 합니다."
  )
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    User user = authService.signup(
        request.email(),
        request.password(),
        request.username(),
        request.phoneNumber()
    );

    SignupResponse response = SignupResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .username(user.getUsername())
        .isOnboardingCompleted(user.isOnboardingCompleted())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
  }

  @PostMapping("/login")
  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인합니다. Access Token과 Refresh Token이 발급되며, 판매자 프로필인 경우 storeId와 함께 프로필 상세 정보도 반환됩니다."
  )
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {

    // User-Agent에서 기기 정보 추출
    String deviceInfo = userAgentUtil.extractDeviceInfo(httpRequest);
    log.info("로그인 시도 - 기기 정보: {}", deviceInfo);

    // 로그인 처리 (storeId + 프로필 상세 정보 포함)
    AuthService.LoginResult result = authService.login(
        request.email(),
        request.password(),
        deviceInfo
    );

    User user = result.user();
    List<ProfileType> availableProfiles = profileService.getAvailableProfiles(user.getId());

    // ✅ storeId + 프로필 상세 정보 포함 응답 생성
    LoginResponse response = LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .username(user.getUsername())
        .currentActiveProfileType(user.getCurrentActiveProfileType())
        .currentActiveProfileId(user.getCurrentActiveProfileId())
        .isOnboardingCompleted(user.isOnboardingCompleted())
        .availableProfiles(availableProfiles)
        .storeId(result.storeId())  // ✅ 상점 ID
        .currentProfileDetail(result.currentProfileDetail())  // ✅ 프로필 상세 정보
        .build();

    // 토큰 설정 (쿠키 + 헤더)
    rq.setAccessToken(result.accessToken());      // 헤더만
    rq.setRefreshToken(result.refreshToken());    // 쿠키만

    return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
  }

  /**
   * 로그아웃 (현재 기기)
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal SecurityUser securityUser,
      @RequestHeader(value = "User-Agent", required = false) String userAgent
  ) {
    String deviceInfo = userAgent != null ? userAgent : "unknown";
    authService.logout(securityUser.getId(), deviceInfo);

    // 쿠키 삭제
    rq.deleteRefreshToken();

    return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
  }

  /**
   * 전체 로그아웃 (모든 기기)
   */
  @PostMapping("/logout/all")
  public ResponseEntity<ApiResponse<Void>> logoutAll(
      @AuthenticationPrincipal SecurityUser securityUser
  ) {
    authService.logoutAll(securityUser.getId());

    // 쿠키 삭제
    rq.deleteRefreshToken();

    return ResponseEntity.ok(ApiResponse.success("전체 로그아웃이 완료되었습니다.", null));
  }

  @PostMapping("/refresh")
  @Operation(
      summary = "토큰 재발급",
      description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
  )
  public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {

    log.info("Access Token 재발급 요청");

    // TokenService를 통해 새 Access Token 발급
    String newAccessToken = tokenService.refreshAccessToken(request.refreshToken());

    RefreshTokenResponse response = RefreshTokenResponse.builder()
        .accessToken(newAccessToken)
        .build();

    // 쿠키 + 응답 헤더에도 설정
    rq.setAccessToken(newAccessToken);

    return ResponseEntity.ok(
        ApiResponse.success("토큰이 재발급되었습니다.", response)
    );
  }
}