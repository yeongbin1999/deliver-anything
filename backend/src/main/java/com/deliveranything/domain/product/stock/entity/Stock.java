package com.deliveranything.domain.product.stock.entity;

import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.global.entity.BaseEntity;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Version;

@Entity
@Getter
@Setter
@Table(name = "stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

  @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
  private Integer quantity = 0;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Version
  private Integer version;

  // 환불시 재고 증가
  public void increaseQuantity(int amount) {
    if (amount < 0) throw new CustomException(ErrorCode.STOCK_CHANGE_INVALID);
    this.quantity += amount;
  }

  // 주문시 재고 감소
  public void decreaseQuantity(int amount) {
    if (amount < 0) throw new CustomException(ErrorCode.STOCK_CHANGE_INVALID);
    if (this.quantity < amount) throw new IllegalArgumentException("재고가 부족합니다.");
    this.quantity -= amount;
  }

  // 관리자용 재고 직접 세팅
  public void setQuantity(int newQuantity) {
    if (newQuantity < 0) throw new CustomException(ErrorCode.STOCK_CHANGE_INVALID);
    this.quantity = newQuantity;
  }

  public Stock(Product product, Integer quantity) {
    this.product = product;
    this.quantity = quantity;
    product.setStock(this);
  }
}