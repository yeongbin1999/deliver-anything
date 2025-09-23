package com.deliveranything.domain.user.service;


import com.deliveranything.domain.user.repository.SellerProfileRepository;
import com.deliveranything.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerProfileService {

  private final UserRepository userRepository;
  private final SellerProfileRepository sellerProfileRepository;
}
