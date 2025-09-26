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
  public List<Review> findReviewsByProfile(User user, ProfileType profileType,
      MyReviewSortType sort,
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
  public List<Review> getStoreReviews(Long storeId, StoreReviewSortType sort,
      String[] decodedCursor, int pageSize) {
    QReview review = QReview.review;

    // 커서 값 초기화
    Long cursorId = null;
    Integer cursorValue = null;          // rating 또는 likeCount 기준
    LocalDateTime cursorCreatedAt = null;

    // decodedCursor가 존재하면 값 파싱
    if (decodedCursor != null && decodedCursor.length > 0) {
      switch (sort) {
        case LATEST:
        case OLDEST:
          cursorCreatedAt = LocalDateTime.parse(decodedCursor[0]);
          cursorId = Long.parseLong(decodedCursor[1]);
          break;
        case RATING_DESC:
        case RATING_ASC:
          cursorValue = Integer.parseInt(decodedCursor[0]);
          cursorId = Long.parseLong(decodedCursor[1]);
          break;
        case LIKED_DESC:
          cursorValue = Integer.parseInt(decodedCursor[0]);
          cursorId = Long.parseLong(decodedCursor[1]);
          break;
      }
    }

    // 커서 조건 생성
    BooleanExpression cursorCondition = null;
    if (cursorId != null) {
      cursorCondition = switch (sort) {
        case LATEST -> review.createdAt.lt(cursorCreatedAt)
            .or(review.createdAt.eq(cursorCreatedAt)
                .and(review.id.lt(cursorId)));
        case OLDEST -> review.createdAt.gt(cursorCreatedAt)
            .or(review.createdAt.eq(cursorCreatedAt)
                .and(review.id.gt(cursorId)));
        case RATING_DESC -> review.rating.lt(cursorValue)
            .or(review.rating.eq(cursorValue)
                .and(review.id.lt(cursorId)));
        case RATING_ASC -> review.rating.gt(cursorValue)
            .or(review.rating.eq(cursorValue)
                .and(review.id.gt(cursorId)));
        case LIKED_DESC -> review.likeCount.lt(cursorValue)
            .or(review.likeCount.eq(cursorValue)
                .and(review.id.lt(cursorId)));
      };
    }

    // 정렬 기준 생성 (복합 정렬)
    OrderSpecifier<?> primary = null, secondary = null;

    switch (sort) {
      case LATEST:
        primary = review.createdAt.desc();
        secondary = review.id.desc();
        break;
      case OLDEST:
        primary = review.createdAt.asc();
        secondary = review.id.asc();
        break;
      case RATING_DESC:
        primary = review.rating.desc();
        secondary = review.id.desc();
        break;
      case RATING_ASC:
        primary = review.rating.asc();
        secondary = review.id.asc();
        break;
      case LIKED_DESC:
        primary = review.likeCount.desc();
        secondary = review.id.desc();
        break;
    }

    // 실제 조회
    return queryFactory
        .selectFrom(review)
        .where(review.targetId.eq(storeId), cursorCondition)
        .orderBy(primary, secondary)
        .limit(pageSize + 1)
        .fetch();
  }
}
