package com.deliveranything.domain.product.entity;

import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_products_name_per_store", columnNames = {"store_id","name"})
    },
    indexes = {
        @Index(name = "idx_products_store_status_created", columnList = "store_id, status, created_at, id"),
        @Index(name = "idx_products_store_status_price",   columnList = "store_id, status, price, id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_store"))
  private Store store;

//  추후 도입 고민 가게 업종 분류에 따라 물품 카테고리도 달라짐 & 일반 소매점에서 검색 외 카테고리까지 필요한지
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "product_category_id",
//      foreignKey = @ForeignKey(name = "fk_products_product_category"))
//  private ProductCategory productCategory;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Integer price;

  @Column(name = "primary_image_url", nullable = false, length = 512)
  private String primaryImageUrl;

  //  popularScore = (최근30일 판매량 × 가중치1) + (최근30일 매출액 × 가중치2) 계획
  @Column(name = "popular_score", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
  private Double popularScore = 0.0;

  public Product(Store store, String name, Integer price, String primaryImageUrl) {
    this.store = store;
    this.name = name;
    this.price = price;
    this.primaryImageUrl = primaryImageUrl;
  }

//  public void changeCategory(ProductCategory category) {
//    this.productCategory = category;
//  }
}