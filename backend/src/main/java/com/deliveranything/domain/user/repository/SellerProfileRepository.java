package com.deliveranything.domain.user.repository;

import com.deliveranything.domain.user.entity.profile.SellerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

  boolean existsByBusinessCertificateNumber(String businessCertificateNumber);

  Optional<SellerProfile> findByBusinessCertificateNumber(String businessCertificateNumber);

  @Query("SELECT sp FROM SellerProfile sp WHERE sp.user.id = :userId")
  Optional<SellerProfile> findByUserId(@Param("userId") Long userId);
}
