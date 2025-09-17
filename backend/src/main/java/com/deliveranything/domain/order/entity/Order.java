package com.deliveranything.domain.order.entity;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id")
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User consumer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_id")
  private Settlement settlement;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_id", unique = true)
  private Delivery delivery;

  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  private OrderStatus orderStatus;

  private UUID merchantId;

  private String address;
  private String riderNote;
  private String storeNote;

  @Column(precision = 9)
  private BigDecimal totalPrice;
  @Column(precision = 9)
  private BigDecimal storePrice;
  @Column(precision = 9)
  private BigDecimal deliveryPrice;
}
