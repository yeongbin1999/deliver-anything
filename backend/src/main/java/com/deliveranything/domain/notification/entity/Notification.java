package com.deliveranything.domain.notification.entity;

import com.deliveranything.domain.notification.enums.NotificationType;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "notifications",
    indexes = @Index(name = "idx_notification_recipient_id_created_at", columnList = "recipientId, createdAt")
)
public class Notification extends BaseEntity {

  @Column(nullable = false)
  private Long recipientId; // Profile ID

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationType type;

  @Column(nullable = false, length = 255)
  private String message;

  @Column(length = 500)
  private String data; // JSON

  @Column(nullable = false)
  private boolean isRead = false;

}

/**
 *
 * {
 *   "type": "NEW_ORDER",
 *   "message": "새 주문이 도착했습니다.",
 *   "data": {"orderId": 12345, "items": [{"name":"치킨","qty":2}]}
 * }
 */