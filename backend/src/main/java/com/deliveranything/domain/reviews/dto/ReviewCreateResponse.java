package com.deliveranything.domain.reviews.dto;

import com.deliveranything.domain.reviews.enums.ReviewTargetType;

public record ReviewCreateResponse(
   Long id,
   int rating,
   String comment,
   String[] photoUrl,
   //유저 dto. id, 닉네임, 프로필이미지 포함
   //dto 생성 시 주석 해제
//   UserSummary user,
   ReviewTargetType targetType,
   Long targetId
) {}
