package com.deliveranything.domain.review.factory;

import com.deliveranything.domain.review.dto.ReviewCreateRequest;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import java.util.ArrayList;
import java.util.List;

public class ReviewFactory {

  //리뷰 생성용 factory class
  public static List<ReviewCreateRequest> createReviews(int count) {
    List<ReviewCreateRequest> reviewCreateRequests = new ArrayList<>();

    for (int i = 1; i <= count; i++) {
      ReviewCreateRequest request = new ReviewCreateRequest(
          i % 5,
          "test comment" + i,
          new String[]{"testUrl" + i},
          ReviewTargetType.STORE,
          (long) i
      );

      reviewCreateRequests.add(request);
    }

    return reviewCreateRequests;
  }
}
