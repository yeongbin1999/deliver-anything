package com.deliveranything.domain.review.repository;

import com.deliveranything.domain.review.entity.Review;
import com.deliveranything.domain.review.enums.ReviewSortType;
import com.deliveranything.domain.review.repository.ReviewRepositoryCustom;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.enums.ProfileType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Review> findReviewsByProfile(User user, ProfileType profileType, ReviewSortType sort, Long cursor, int pageSize) {
    QReview review = QReview.review;

    BooleanExpression profileCondition = switch (profileType) {
      case CUSTOMER -> review.customer.eq(user);
      case SELLER -> review.seller.eq(user);
      case RIDER -> review.rider.eq(user);
    };

    BooleanExpression cursorCondition = null;
    if (cursor != null) {
      cursorCondition = switch (sort) {
        case LATEST -> review.id.lt(cursor);   // 최신순
        case OLDEST -> review.id.gt(cursor);   // 오래된순
      };
    }

    OrderSpecifier<?> orderSpecifier = switch (sort) {
      case LATEST -> review.createdAt.desc();
      case OLDEST -> review.createdAt.asc();
    };

    return queryFactory
        .selectFrom(review)
        .where(profileCondition, cursorCondition)
        .orderBy(orderSpecifier)
        .limit(pageSize + 1)  // hasNext 판단 위해 +1
        .fetch();
  }
}
