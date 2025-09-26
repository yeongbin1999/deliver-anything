package com.deliveranything.domain.product.stock.entity;

import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "stocks")
public class Stock extends BaseEntity {

  @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
  private Integer quantity = 0;

}
