package com.deliveranything.domain.user.service;

import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.repository.RiderProfileRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RiderProfileService {

  // CQRS 패턴 적용하여 클래스/네이밍 분리하려 했으나 이는 추후 고려

  private final RiderProfileRepository riderProfileRepository;

  public RiderProfile getRiderProfileById(Long riderProfileId) {
    return riderProfileRepository.findById(riderProfileId)
        .orElseThrow(() -> new CustomException(ErrorCode.RIDER_NOT_FOUND));
  }

}
