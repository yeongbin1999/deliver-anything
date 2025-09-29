package com.deliveranything.domain.product.stock.entity;

import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

  @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
  private Integer quantity = 0;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  public Stock(Product product, Integer quantity) {
    this.product = product;
    this.quantity = quantity;
    product.setStock(this);
  }
}