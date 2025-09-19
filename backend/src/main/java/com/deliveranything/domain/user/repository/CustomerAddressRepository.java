package com.deliveranything.domain.user.repository;

import com.deliveranything.domain.user.entity.address.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
  
}
