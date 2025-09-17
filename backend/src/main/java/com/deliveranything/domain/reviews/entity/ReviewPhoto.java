package com.deliveranything.domain.reviews.entity;

import com.deliveranything.global.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
public class ReviewPhoto extends BaseEntity {

  @Schema(description = "리뷰 객체")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Schema(description = "사진 URL")
  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @LastModifiedDate
  @Schema(description = "수정 일시")
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Schema(description = "삭제 여부")
  @Column(name = "is_deleted")
  private boolean isDeleted = false;
}
