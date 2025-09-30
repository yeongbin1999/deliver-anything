package com.deliveranything.domain.product.stock.repository;

import com.deliveranything.domain.product.stock.entity.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findByProductId(Long productId);
}
