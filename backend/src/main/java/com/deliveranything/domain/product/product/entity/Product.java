package com.deliveranything.domain.product.product.entity;

import com.deliveranything.domain.product.product.dto.ProductCreateRequest;
import com.deliveranything.domain.product.stock.entity.Stock;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
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

  @Column(nullable = false, length = 120)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Integer price;

  @Column(name = "image_url", nullable = false, length = 512)
  private String imageUrl;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "product_keywords",
      joinColumns = @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_keywords_product")),
      indexes = {
          @Index(name = "idx_product_keywords_keyword", columnList = "keyword")
      }
  )
  private List<String> keywords = new ArrayList<>();

  @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Stock stock;



  public static Product of(ProductCreateRequest request, Store store) {
    return new Product();
  }
}