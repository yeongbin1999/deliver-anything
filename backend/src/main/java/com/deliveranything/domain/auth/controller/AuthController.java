package com.deliveranything.domain.auth.controller;

import com.deliveranything.domain.auth.dto.LoginRequest;
import com.deliveranything.domain.auth.dto.LoginResponse;
import com.deliveranything.domain.auth.dto.SignupRequest;
import com.deliveranything.domain.auth.dto.SignupResponse;
import com.deliveranything.domain.auth.service.AuthService;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.service.ProfileService;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.domain.user.user.service.UserService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.Rq;
import com.deliveranything.global.util.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;
  private final ProfileService profileService;
  private final UserAgentUtil userAgentUtil;

  private final Rq rq;

  /**
   * 회원가입 POST /api/v1/auth/signup
   */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    User user = authService.signup(
        request.email(),
        request.password(),
        request.name(),
        request.phoneNumber()
    );

    SignupResponse response = SignupResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .isOnboardingCompleted(user.isOnboardingCompleted())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
  }

  /**
   * 로그인 POST /api/v1/auth/login
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {  // HttpServletRequest 추가

    // User-Agent에서 기기 정보 추출
    String deviceInfo = userAgentUtil.extractDeviceInfo(httpRequest);
    log.info("로그인 시도 - 기기 정보: {}", deviceInfo);

    // 로그인 처리
    AuthService.LoginResult result = authService.login(
        request.email(),
        request.password(),
        deviceInfo  // 추출한 기기 정보 전달
    );

    User user = result.user();
    List<ProfileType> availableProfiles = profileService.getAvailableProfiles(user.getId());

    // 응답 생성
    LoginResponse response = LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .currentActiveProfileType(user.getCurrentActiveProfileType())
        .currentActiveProfileId(user.getCurrentActiveProfileId())
        .isOnboardingCompleted(user.isOnboardingCompleted())
        .availableProfiles(availableProfiles)
        .build();

    // 토큰을 쿠키와 헤더에 설정
    rq.setAccessToken(result.accessToken());
    rq.setApiKey(result.refreshToken());

    return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout() {
    User currentUser = rq.getActor();
    if (currentUser != null) {
      authService.logout(currentUser.getId());
    }

    // 쿠키 삭제
    rq.deleteCookie("accessToken");
    rq.deleteCookie("apiKey");

    return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
  }
}