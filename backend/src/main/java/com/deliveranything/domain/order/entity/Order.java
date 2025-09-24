package com.deliveranything.domain.order.entity;

import com.deliveranything.domain.delivery.entity.Delivery;
import com.deliveranything.domain.order.enums.OrderStatus;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private CustomerProfile customer;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_id", unique = true)
  private Delivery delivery;

  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false, unique = true, length = 200)
  private String merchantId;

  @Column(nullable = false, length = 100)
  private String address;

  @Column(length = 30)
  private String riderNote;

  @Column(length = 30)
  private String storeNote;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal storePrice;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal deliveryPrice;

  @Builder
  public Order(CustomerProfile customer, Store store, String address, String riderNote,
      String storeNote, BigDecimal totalPrice, BigDecimal storePrice, BigDecimal deliveryPrice) {
    this.customer = customer;
    this.store = store;
    this.address = address;
    this.riderNote = riderNote;
    this.storeNote = storeNote;
    this.totalPrice = totalPrice;
    this.storePrice = storePrice;
    this.deliveryPrice = deliveryPrice;
    this.status = OrderStatus.PENDING;
    this.merchantId = UUID.randomUUID().toString();
  }

  public void addOrderItem(OrderItem orderItem) {
    this.orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void updateStatus(OrderStatus status) {
    if (!this.status.canTransitTo(status)) {
      // TODO: 백엔드 내부에서 주문 상태에 접근하므로 log 만 후에 기록한다. 프론트는 해석만 할 뿐 주문 상태들을 건드릴 필요 없음.
    }

    this.status = status;
  }
}
