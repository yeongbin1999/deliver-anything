package com.deliveranything.domain.review.repository;

import com.deliveranything.domain.review.entity.QReview;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.MyReviewSortType;
import com.deliveranything.domain.review.enums.StoreReviewSortType;
import com.deliveranything.domain.store.store.entity.QStore;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.entity.profile.QRiderProfile;
import com.deliveranything.domain.user.enums.ProfileType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Review> findReviewsByProfile(User user, ProfileType profileType, MyReviewSortType sort,
      String[] cursor, int pageSize) {
    QReview review = QReview.review;
    QRiderProfile riderProfile = QRiderProfile.riderProfile;
    QStore store = QStore.store;

    // profileType에 따른 조건
    BooleanExpression profileCondition = switch (profileType) {
      case CUSTOMER -> review.customerProfile.eq(user.getCustomerProfile());
      case SELLER -> review.targetId.eq(user.getId());
      case RIDER -> review.targetId.eq(user.getRiderProfile().getId());
    };

    // 커서 조건
    BooleanExpression cursorCondition = null;
    if (cursor != null && cursor.length > 0) {
      cursorCondition = switch (sort) {
        case LATEST -> review.createdAt.lt(LocalDateTime.parse(cursor[0]));
        case OLDEST -> review.createdAt.gt(LocalDateTime.parse(cursor[0]));
        case RATING_DESC -> review.rating.lt(Integer.parseInt(cursor[0]));
        case RATING_ASC -> review.rating.gt(Integer.parseInt(cursor[0]));
      };
    }

    // 정렬
    OrderSpecifier<?> orderSpecifier = switch (sort) {
      case LATEST -> review.createdAt.desc();
      case OLDEST -> review.createdAt.asc();
      case RATING_DESC -> review.rating.desc();
      case RATING_ASC -> review.rating.asc();
    };

    return queryFactory
        .selectFrom(review)
        .where(profileCondition, cursorCondition)
        .orderBy(orderSpecifier)
        .limit(pageSize + 1) // hasNext 판단용 +1
        .fetch();
  }

  @Override
  public void updateLikeCount(Long reviewId, int likeCount) {
    QReview review = QReview.review;

    queryFactory.update(review)
        .set(review.likeCount, likeCount)
        .where(review.id.eq(reviewId))
        .execute();
  }

  @Override
  public List<Review> getStoreReviews(Long storeId, StoreReviewSortType sort, String[] decodedCursor, int size) {

    return List.of();
  }
}
