package com.deliveranything.domain.user.user.repository;

import com.deliveranything.domain.user.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);

  Optional<User> findByApiKey(String apiKey);
}