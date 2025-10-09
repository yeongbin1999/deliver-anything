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

public record AddProfileRequest(
    @NotNull(message = "프로필 타입은 필수입니다.")
    @Schema(description = "프로필 타입", example = "SELLER", requiredMode = Schema.RequiredMode.REQUIRED)
    ProfileType profileType,

    @NotNull(message = "프로필 데이터는 필수입니다.")
    @Valid
    @Schema(
        description = "프로필 데이터 (타입에 따라 구조가 다름)",
        example = """
            {
              "nickname": "홍사장",
              "businessName": "홍길동식당",
              "businessCertificateNumber": "123-45-67890",
              "businessPhoneNumber": "02-1234-5678",
              "bankName": "신한은행",
              "accountNumber": "12345678901234",
              "accountHolder": "홍길동",
              "profileImageUrl": null
            }
            """,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "profileType"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CustomerOnboardingData.class, name = "CUSTOMER"),
        @JsonSubTypes.Type(value = SellerOnboardingData.class, name = "SELLER"),
        @JsonSubTypes.Type(value = RiderOnboardingData.class, name = "RIDER")
    })
    Object profileData
) {

}