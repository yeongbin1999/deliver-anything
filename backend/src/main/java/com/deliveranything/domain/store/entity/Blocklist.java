package com.deliveranything.domain.store.entity;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import com.deliveranything.global.entity.BaseEntity;
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
@Getter
@Table(
    name = "store_blocked_consumers",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_sbc_store_consumer",
        columnNames = {"store_id","consumer_profile_id"}
    ),
    indexes = @Index(name = "idx_sbc_store", columnList = "store_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blocklist extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sbc_store"))
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sbc_profile"))
  private CustomerProfile customerProfile;
}