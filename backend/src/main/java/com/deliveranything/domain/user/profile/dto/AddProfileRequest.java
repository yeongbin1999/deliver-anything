package com.deliveranything.domain.user.profile.dto;

import com.deliveranything.domain.user.profile.dto.onboard.CustomerOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.RiderOnboardingData;
import com.deliveranything.domain.user.profile.dto.onboard.SellerOnboardingData;
import com.deliveranything.domain.user.profile.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 추가 프로필 생성 요청 DTO 온보딩 완료 후 새로운 프로필을 추가할 때 사용
 */
public record AddProfileRequest(
    @NotNull(message = "프로필 타입은 필수입니다.")
    @Schema(description = "생성할 프로필 타입", example = "SELLER")
    ProfileType profileType,

    @NotNull(message = "프로필 데이터는 필수입니다.")
    @Valid
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "profileType" // profileType 필드에 따라 적절한 서브타입으로 매핑
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CustomerOnboardingData.class, name = "CUSTOMER"),
        @JsonSubTypes.Type(value = SellerOnboardingData.class, name = "SELLER"),
        @JsonSubTypes.Type(value = RiderOnboardingData.class, name = "RIDER")
    })
    Object profileData
    // 실제 프로필 상세 데이터 (CustomerOnboardingData, SellerOnboardingData, RiderOnboardingData 중 하나
) {

}