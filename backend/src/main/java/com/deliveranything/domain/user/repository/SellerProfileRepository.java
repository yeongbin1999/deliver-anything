package com.deliveranything.domain.user.repository;

import com.deliveranything.domain.user.entity.profile.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
  
}
