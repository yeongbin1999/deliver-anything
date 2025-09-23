package com.deliveranything.domain.review.scheduler;

import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ReviewLikeSyncScheduler {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ReviewRepository reviewRepository;

  public ReviewLikeSyncScheduler(RedisTemplate<String, Object> redisTemplate,
      ReviewRepository reviewRepository) {
    this.redisTemplate = redisTemplate;
    this.reviewRepository = reviewRepository;
  }

  @Scheduled(fixedRate = 5 * 60 * 1000)
  @Transactional
  public void syncLikesToDb() {
    String reviewSortedKey = "review:likes";

    //모든 리뷰 ID와 score 가져오기
    Set<ZSetOperations.TypedTuple<Object>> tuples =
        redisTemplate.opsForZSet().rangeWithScores(reviewSortedKey, 0, -1);

    if (tuples != null) {
      for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
        Long reviewId = (Long) tuple.getValue();
        Double score = tuple.getScore();

        if (score == null) {
          log.warn("리뷰 {}: 좋아요 점수가 없습니다.", reviewId);
          continue;
        }
        int likeCount = score.intValue();

        try {
          reviewRepository.updateLikeCount(reviewId, likeCount);
          log.info("리뷰 {}: 좋아요 수를 {}로 업데이트했습니다.", reviewId, likeCount);
        } catch (Exception e) {
          log.error("리뷰 {}, 좋아요 수 {}: 업데이트에 실패했습니다.", reviewId, likeCount, e);
        }
        //TODO: 추후 카프카 이벤트 발행 고려
      }
    }
  }

}
