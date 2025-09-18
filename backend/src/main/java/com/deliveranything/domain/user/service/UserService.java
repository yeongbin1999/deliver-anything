package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.User;
import com.deliveranything.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  // 기본 조회 Methods
  public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public Optional<User> findByIdOptional(Long id) {
    return userRepository.findById(id);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  // 중복 체크 Methods
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return userRepository.existsByPhoneNumber(phoneNumber);
  }

  // 기본 정보 수정 Methods
  @Transactional
  public void updateBasicInfo(Long userId, String name, String phoneNumber) {
    User user = findById(userId);
    if (user == null) {
      log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
      return;
    }
    // 핸드폰 번호 중복 체크 (본인 제외)
    if (!user.getPhoneNumber().equals(phoneNumber) &&
        existsByPhoneNumber(phoneNumber)) {
      log.warn("이미 사용 중인 전화번호입니다: phoneNumber={}", phoneNumber);
      return;
    }

    user.updateBasicInfo(name, phoneNumber);
    userRepository.save(user);

    log.info("사용자 기본 정보 업데이트 완료: userId={}", userId);
  }


}
