package com.deliveranything.domain.user.repository;

import com.deliveranything.domain.user.entity.profile.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

}
