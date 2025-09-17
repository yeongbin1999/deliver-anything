package com.deliveranything.domain.reviews.entity; // id, rating, comment, target_type, target_id, user_id, created_at, updated_at, deleted_at, is_deleted

import com.deliveranything.domain.reviews.enums.ReviewTargetType;
import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Entity
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "리뷰 ID")
  private Long id;

  @Schema(description = "별점")
  private int rating;

  @Schema(description = "리뷰 코멘트")
  private String comment;

  @Schema(description = "대상 타입(상점 or 배달원)")
  @Enumerated(EnumType.STRING)
  private ReviewTargetType targetType;

  @Schema(description = "대상 ID (상점 or 배달원)")
  private Long targetId;

  // 작성자 id
  // User 객체가 생성될 시 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

  @Schema(description = "생성 일시")
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Schema(description = "수정 일시")
  private LocalDateTime updatedAt;

  @Schema(description = "삭제 일시")
  private LocalDateTime deletedAt;

  @Schema(description = "삭제 상태")
  private boolean isDeleted = false;

  //==================생성 메소드===================
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
