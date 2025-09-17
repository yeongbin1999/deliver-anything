package com.deliveranything.domain.product.entity;

import com.deliveranything.domain.store.entity.Store;
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
  @JoinColumn(name = "store_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_products_store"))
  private Store store;

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

  @Column(name = "popular_score")
  private Double popularScore;

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