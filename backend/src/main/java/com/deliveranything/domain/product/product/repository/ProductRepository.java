package com.deliveranything.domain.product.product.repository;

import com.deliveranything.domain.product.product.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

  List<Product> findAllByStoreId(Long storeId);
}
