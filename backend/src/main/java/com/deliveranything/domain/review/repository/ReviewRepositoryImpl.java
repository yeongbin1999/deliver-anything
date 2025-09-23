package com.deliveranything.domain.review.repository;

import com.deliveranything.domain.review.entity.QReview;
import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.review.enums.ReviewTargetType;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.ProfileType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  // ==========================
  // 리뷰 조회 - 사용자 프로필 기준
  // ==========================
  @Override
  public List<Review> findReviewsByProfile(User user, ProfileType profileType, ReviewSortType sort,
      String[] cursor, int pageSize) {
    QReview review = QReview.review;

    BooleanBuilder builder = new BooleanBuilder();

    // profileType 조건
    switch (profileType) {
      case CUSTOMER -> builder.and(review.customerProfile.eq(user.getCustomerProfile()));
      case SELLER -> builder.and(review.targetId.eq(user.getId()));
      case RIDER -> builder.and(review.targetId.eq(user.getRiderProfile().getId()));
    }

    // 커서 조건 추가
    builder.and(buildCursorCondition(sort, cursor));

    return queryFactory
        .selectFrom(review)
        .where(builder)
        .orderBy(getOrderSpecifier(sort))
        .limit(pageSize + 1) // hasNext 체크용
        .fetch();
  }

  // ==========================
  // 리뷰 조회 - 상점 기준
  // ==========================
  @Override
  public List<Review> findReviewsByStore(Store store, ReviewSortType sort, String[] cursor, int size) {
    QReview review = QReview.review;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(review.targetType.eq(ReviewTargetType.STORE));
    builder.and(review.targetId.eq(store.getId()));

    // 커서 조건 추가
    builder.and(buildCursorCondition(sort, cursor));

    return queryFactory
        .selectFrom(review)
        .where(builder)
        .orderBy(getOrderSpecifier(sort))
        .limit(size + 1) // hasNext 체크용
        .fetch();
  }

  // ==========================
  // 커서 조건 공통 메서드
  // ==========================
  private BooleanBuilder buildCursorCondition(ReviewSortType sort, String[] cursor) {
    BooleanBuilder builder = new BooleanBuilder();
    QReview review = QReview.review;

    if (cursor != null && cursor.length == 2) {
      switch (sort) {
        case LATEST -> builder.and(
            review.createdAt.lt(LocalDateTime.parse(cursor[0]))
                .or(review.createdAt.eq(LocalDateTime.parse(cursor[0]))
                    .and(review.id.lt(Long.parseLong(cursor[1]))))
        );
        case OLDEST -> builder.and(
            review.createdAt.gt(LocalDateTime.parse(cursor[0]))
                .or(review.createdAt.eq(LocalDateTime.parse(cursor[0]))
                    .and(review.id.gt(Long.parseLong(cursor[1]))))
        );
        case RATING_DESC -> builder.and(
            review.rating.lt(Integer.parseInt(cursor[0]))
                .or(review.rating.eq(Integer.parseInt(cursor[0]))
                    .and(review.id.lt(Long.parseLong(cursor[1]))))
        );
        case RATING_ASC -> builder.and(
            review.rating.gt(Integer.parseInt(cursor[0]))
                .or(review.rating.eq(Integer.parseInt(cursor[0]))
                    .and(review.id.gt(Long.parseLong(cursor[1]))))
        );
      }
    }

    return builder;
  }

  // ==========================
  // 정렬 기준 공통 메서드
  // ==========================
  private OrderSpecifier<?>[] getOrderSpecifier(ReviewSortType sort) {
    QReview review = QReview.review;

    return switch (sort) {
      case LATEST -> new OrderSpecifier[]{review.createdAt.desc().nullsLast(), review.id.desc()};
      case OLDEST -> new OrderSpecifier[]{review.createdAt.asc().nullsLast(), review.id.asc()};
      case RATING_DESC -> new OrderSpecifier[]{review.rating.desc(), review.id.desc()};
      case RATING_ASC -> new OrderSpecifier[]{review.rating.asc(), review.id.asc()};
    };
  }

}
