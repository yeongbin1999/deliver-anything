package com.deliveranything.domain.reviews.entity;

import com.deliveranything.domain.reviews.enums.ReviewTargetType;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "review")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

  @Schema(description = "별점")
  @Column(nullable = false)
  private int rating;

  @Schema(description = "리뷰 코멘트")
  @Column(length = 1000)
  private String comment;

  @Schema(description = "대상 타입(상점 or 배달원)")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReviewTargetType targetType;

  @Schema(description = "대상 ID (상점 or 배달원)")
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Schema(description = "작성자 객체")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @LastModifiedDate
  @Schema(description = "수정 일시")
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Schema(description = "삭제 일시")
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Schema(description = "삭제 상태")
  @Column(name = "is_deleted")
  private boolean isDeleted = false;

  //========================생성 메소드===========================
  @Builder
  public Review(
      int rating,
      String comment,
      ReviewTargetType targetType,
      Long targetId,
      User user) {
    this.rating = rating;
    this.comment = comment;
    this.targetType = targetType;
    this.targetId = targetId;
    this.user = user;
  }
}
