package com.deliveranything.domain.user.profile.dto;

import com.deliveranything.domain.user.profile.dto.onboard.CustomerOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.RiderOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.SellerOnboardingData;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 온보딩 요청 DTO 프로필 타입에 따라 다른 데이터를 받을 수 있도록 다형성 지원
 */
public record OnboardingRequest(
    @NotNull(message = "프로필 타입은 필수입니다.")
    ProfileType selectedProfile,

    @NotNull(message = "프로필 데이터는 필수입니다.")
    @Valid  // 중첩된 객체도 검증
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "selectedProfile"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CustomerOnboardingData.class, name = "CUSTOMER"),
        @JsonSubTypes.Type(value = SellerOnboardingData.class, name = "SELLER"),
        @JsonSubTypes.Type(value = RiderOnboardingData.class, name = "RIDER")
    })
    Object profileData  // CustomerOnboardingData | SellerOnboardingData | RiderOnboardingData
) {

}