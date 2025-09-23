package com.deliveranything.domain.user.repository;

import com.deliveranything.domain.user.entity.address.CustomerAddress;
import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

  @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customerProfile = :profile ORDER BY ca.id ASC")
  List<CustomerAddress> findAddressesByProfile(@Param("profile") CustomerProfile profile);

  long countByCustomerProfile(CustomerProfile profile);
}
