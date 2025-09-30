package com.deliveranything.domain.notification.scheduler;

import com.deliveranything.domain.notification.repository.NotificationRepository;
import com.deliveranything.domain.notification.service.NotificationService;
import com.deliveranything.domain.review.repository.ReviewRepository;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.repository.RiderProfileRepository;
import com.deliveranything.domain.user.repository.SellerProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import com.deliveranything.domain.user.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

  private final NotificationService notificationService;
  private final ReviewRepository reviewRepository;
  private final SellerProfileRepository sellerProfileRepository;
  private final RiderProfileRepository riderProfileRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final NotificationRepository notificationRepository;
  private final RedisTemplate<String, String> redisTemplate;

  // 1시간마다 실행
  @Scheduled(cron = "0 0 * * * *")
  public void sendReviewNotificationsHourly() {
    log.info("1시간 리뷰 알림 스케줄 시작");

    // 1. SMS 발송 대상 사용자 조회
    List<User> allUsers = userService.findAllSellersAndRiders();

    for (User user : allUsers) {
      String redisKey = "notifications:hourly:" + user.getPhoneNumber();

      // 2. Redis 에서 targetType별 알림 수 조회
      Map<Object, Object> counts = redisTemplate.opsForHash().entries(redisKey);

      if (!counts.isEmpty()) {
        // 3. SMS 메시지 생성
        StringBuilder message = new StringBuilder("지난 1시간 동안 새 리뷰: ");
        counts.forEach((type, count) -> message.append(type).append(" ").append(count).append("건, "));

        // 마지막 쉼표 제거
        if (message.charAt(message.length() - 2) == ',') {
          message.setLength(message.length() - 2);
        }

        try {
          // 4. SMS 발송
          smsService.sendSms(user.getPhoneNumber(), message.toString());
          log.info("SMS 전송 완료: {}, 내용: {}", user.getPhoneNumber(), message);

          // 5. Redis 초기화 (중복 방지)
          redisTemplate.delete(redisKey);

        } catch (Exception e) {
          log.error("SMS 전송 실패: {}", user.getPhoneNumber(), e);
        }
      }
    }

    log.info("===== 1시간 단위 리뷰 알림 스케줄러 종료 =====");
  }
}
