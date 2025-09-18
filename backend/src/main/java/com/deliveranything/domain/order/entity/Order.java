package com.deliveranything.domain.order.entity;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.store.entity.Store;
import com.deliveranything.domain.user.entity.User;
import com.deliveranything.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User customer;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_id", unique = true)
  private Delivery delivery;

  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus orderStatus;

  @Column(nullable = false)
  private UUID merchantId;

  @Column(nullable = false)
  private String address;

  private String riderNote;
  private String storeNote;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal storePrice;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal deliveryPrice;
}
