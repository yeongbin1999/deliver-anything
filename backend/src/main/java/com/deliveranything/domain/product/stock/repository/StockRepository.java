package com.deliveranything.domain.product.stock.repository;

import com.deliveranything.domain.product.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

}
