package com.deliveranything.domain.store.categoty.entity;

import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "store_categories",
    indexes = @Index(name = "idx_store_categories_sort", columnList = "sort_order"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCategory extends BaseEntity {

  @Column(nullable = false, length = 80)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  /** 시드 신규 생성용 */
  public static StoreCategory create(String name, int sortOrder) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
    StoreCategory c = new StoreCategory();
    c.name = name;
    c.sortOrder = sortOrder;
    c.isActive = true;
    return c;
  }

  /** 시드 재적용(존재 시 갱신) */
  public void reseed(int sortOrder, boolean active) {
    this.sortOrder = sortOrder;
    this.isActive = active;
  }
}