package com.deliveranything.domain.user.controller.auth;

import com.deliveranything.domain.user.dto.auth.LoginRequest;
import com.deliveranything.domain.user.dto.auth.LoginResponse;
import com.deliveranything.domain.user.dto.auth.SignupRequest;
import com.deliveranything.domain.user.dto.auth.SignupResponse;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.token.RefreshToken;
import com.deliveranything.domain.user.enums.ProfileType;
import com.deliveranything.domain.user.service.UserService;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.Rq;
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

  private final UserService userService;
  private final Rq rq;

  /**
   * 회원가입 POST /api/v1/auth/signup
   */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    User user = userService.signup(
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
      @Valid @RequestBody LoginRequest request) {
    // 로그인 처리
    User user = userService.login(request.email(), request.password());

    // Access Token 생성
    String accessToken = userService.genAccessToken(user);

    // Refresh Token 생성
    String deviceInfo = extractDeviceInfo();
    RefreshToken refreshToken = userService.createRefreshToken(user, deviceInfo);

    // 사용 가능한 프로필 목록 조회
    List<ProfileType> availableProfiles = userService.getAvailableProfiles(user.getId());

    // 응답 생성
    LoginResponse response = LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .accessToken(accessToken)
        .refreshToken(refreshToken.getTokenValue())
        .currentActiveProfileType(user.getCurrentActiveProfileType())
        .currentActiveProfileId(user.getCurrentActiveProfileId())
        .isOnboardingCompleted(user.isOnboardingCompleted())
        .availableProfiles(availableProfiles)
        .build();

    // 토큰을 쿠키와 헤더에 설정
    rq.setAccessToken(accessToken);
    rq.setApiKey(refreshToken.getTokenValue());

    return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout() {
    User currentUser = rq.getActor();
    if (currentUser != null) {
      userService.invalidateAllRefreshTokens(currentUser.getId());
    }

    // 쿠키 삭제
    rq.deleteCookie("accessToken");
    rq.deleteCookie("apiKey");

    return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
  }

  private String extractDeviceInfo() {
    // 실제 구현시에는 HttpServletRequest에서 User-Agent 등을 추출
    return "Web Browser"; // 임시
  }

}