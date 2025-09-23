package com.deliveranything.domain.review.scheduler;

import com.deliveranything.domain.review.dto.ReviewLikeEvent;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReviewLikeSyncScheduler {

  private final RedisTemplate<String, Object> redisTemplate;

  public ReviewLikeSyncScheduler(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void syncLikesToDb() {
    String reviewSortedKey = "review:likes";

    //모든 리뷰 ID와 score 가져오기
    Set<ZSetOperations.TypedTuple<Object>> tuples =
        redisTemplate.opsForZSet().rangeWithScores(reviewSortedKey, 0, -1);

    if (tuples != null) {
      for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
        Long reviewId = (Long) tuple.getValue();
        Long likeCount =  tuple.getScore().longValue();

        ReviewLikeEvent event = new ReviewLikeEvent(reviewId, likeCount);
        kafkaTemplate.send("review-like-topic", event);
      }
    }
  }

}
