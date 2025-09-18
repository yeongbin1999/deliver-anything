//package com.deliveranything.domain.product.category.entity;
//
//import com.deliveranything.global.entity.BaseEntity;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EntityListeners;
//import jakarta.persistence.Table;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//@Entity
//@Getter
//@Table(name = "product_categories")
//@EntityListeners(AuditingEntityListener.class)
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class ProductCategory extends BaseEntity {
//
//  @Column(nullable = false, length = 80)
//  private String name;
//
//  @Column(nullable = false, length = 255)
//  private String path;
//
//  @Column(
//      name = "depth",
//      columnDefinition = "TINYINT GENERATED ALWAYS AS ((LENGTH(path) - LENGTH(REPLACE(path,'/',''))) - 1) STORED",
//      insertable = false, updatable = false
//  )
//  private Byte depth;
//
//  @Column(name = "sort_order", nullable = false)
//  private int sortOrder = 0;
//
//  @Column(name = "is_active", nullable = false)
//  private boolean isActive = true;
//
//  public ProductCategory(String name, String path, int sortOrder, boolean isActive) {
//    this.name = name;
//    this.path = path;
//    this.sortOrder = sortOrder;
//    this.isActive = isActive;
//  }
//}
