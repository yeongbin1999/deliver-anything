package com.deliveranything.domain.store.category.entity;

import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.jcip.annotations.Immutable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "store_categories")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable
public class StoreCategory extends BaseEntity {

  @Column(nullable = false, length = 30)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  // 시드용 생성자
  public StoreCategory(String name, int sortOrder, boolean isActive) {
    this.name = name;
    this.sortOrder = sortOrder;
    this.isActive = isActive;
  }
}
