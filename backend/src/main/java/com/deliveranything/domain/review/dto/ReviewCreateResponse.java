package com.deliveranything.domain.review.dto;

import com.deliveranything.domain.review.enums.ReviewTargetType;
import java.util.List;

public record ReviewCreateResponse(
    Long id,
    int rating,
    String comment,
    List<String> photoUrl,
    //유저 dto. id, 닉네임, 프로필이미지 포함
    //dto 생성 시 주석 해제
//   UserSummary user,
    ReviewTargetType targetType,
    Long targetId
) {

}
