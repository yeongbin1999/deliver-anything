package com.deliveranything.domain.review.repository;

import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.ProfileType;
import java.util.List;

public interface ReviewRepositoryCustom {

  List<Review> findReviewsByProfile(User user, ProfileType profileType, ReviewSortType sort,
      String[] cursor, int size);

  List<Review> findReviewsByStore(Store store, ReviewSortType sort, String[] cursor, int size);
}
