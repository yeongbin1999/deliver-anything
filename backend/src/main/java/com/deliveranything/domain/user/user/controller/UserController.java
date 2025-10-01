package com.deliveranything.domain.user.user.controller;

import com.deliveranything.domain.auth.service.AuthService;
import com.deliveranything.domain.user.profile.dto.AvailableProfilesResponse;
import com.deliveranything.domain.user.profile.dto.OnboardingRequest;
import com.deliveranything.domain.user.profile.dto.OnboardingResponse;
import com.deliveranything.domain.user.profile.dto.SwitchProfileRequest;
import com.deliveranything.domain.user.profile.dto.SwitchProfileResponse;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.deliveranything.domain.user.profile.service.ProfileService;
import com.deliveranything.domain.user.user.entity.User;
import com.deliveranything.global.common.ApiResponse;
import com.deliveranything.global.common.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 프로필 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

  private final ProfileService profileService;
  private final AuthService authService;
  private final Rq rq;

  @PostMapping("/onboarding")
  @Operation(
      summary = "온보딩 완료",
      description = "회원가입 후 첫 프로필을 생성하고 온보딩을 완료합니다. CUSTOMER, SELLER, RIDER 중 하나를 선택할 수 있습니다."
  )
  public ResponseEntity<ApiResponse<OnboardingResponse>> completeOnboarding(
      @Valid @RequestBody OnboardingRequest request) {

    User currentUser = rq.getActor();
    log.info("온보딩 요청: userId={}, selectedProfile={}",
        currentUser.getId(), request.selectedProfile());

    boolean success = profileService.completeOnboarding(
        currentUser.getId(),
        request.selectedProfile(),
        request.profileData()
    );

    if (!success) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.fail("ONBOARDING-001", "온보딩을 완료할 수 없습니다."));
    }

    // 온보딩 완료 후 사용자 정보 다시 조회
    User updatedUser = rq.getActor();

    OnboardingResponse response = OnboardingResponse.builder()
        .userId(updatedUser.getId())
        .selectedProfile(request.selectedProfile())
        .profileId(updatedUser.getCurrentActiveProfileId())
        .isOnboardingCompleted(true)
        .build();

    return ResponseEntity.ok(
        ApiResponse.success("온보딩이 완료되었습니다.", response)
    );
  }

  @PostMapping("/profile/switch")
  @Operation(
      summary = "프로필 전환",
      description = "사용자가 보유한 다른 프로필로 전환합니다. 프로필 전환 시 새로운 Access Token이 자동으로 발급됩니다."
  )
  public ResponseEntity<ApiResponse<SwitchProfileResponse>> switchProfile(
      @Valid @RequestBody SwitchProfileRequest request) {

    User currentUser = rq.getActor();
    log.info("프로필 전환 요청: userId={}, targetProfile={}",
        currentUser.getId(), request.targetProfileType());

    // AuthService가 프로필 전환 + 토큰 재발급 orchestration
    SwitchProfileResponse result = authService.switchProfileWithTokenReissue(
        currentUser.getId(),
        request.targetProfileType()
    );

    // 새 Access Token을 쿠키와 헤더에 설정
    rq.setAccessToken(result.accessToken());

    // API 응답용으로 변환 (토큰 제거)
    SwitchProfileResponse response = result.toResponse();

    return ResponseEntity.ok(
        ApiResponse.success("프로필이 전환되었습니다.", response)
    );
  }

  @GetMapping("/profiles")
  @Operation(
      summary = "사용 가능한 프로필 목록 조회",
      description = "현재 사용자가 보유한 모든 활성 프로필 목록과 현재 활성화된 프로필을 조회합니다."
  )
  public ResponseEntity<ApiResponse<AvailableProfilesResponse>> getAvailableProfiles() {

    User currentUser = rq.getActor();
    log.info("사용 가능한 프로필 조회: userId={}", currentUser.getId());

    List<ProfileType> availableProfiles = profileService.getAvailableProfiles(
        currentUser.getId()
    );

    AvailableProfilesResponse response = AvailableProfilesResponse.builder()
        .userId(currentUser.getId())
        .availableProfiles(availableProfiles)
        .currentActiveProfile(currentUser.getCurrentActiveProfileType())
        .build();

    return ResponseEntity.ok(
        ApiResponse.success("프로필 목록 조회 완료", response)
    );
  }
}