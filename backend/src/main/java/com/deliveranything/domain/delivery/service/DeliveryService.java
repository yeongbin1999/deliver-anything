package com.deliveranything.domain.delivery.service;

import com.deliveranything.domain.delivery.dto.request.RiderToggleStatusRequestDto;
import com.deliveranything.domain.user.entity.profile.RiderProfile;
import com.deliveranything.domain.user.enums.RiderToggleStatus;
import com.deliveranything.domain.user.repository.RiderProfileRepository;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final RiderProfileRepository riderProfileRepository;

  public void updateRiderStatus(RiderToggleStatusRequestDto riderStatusRequestDto) {
    // 라이더 상태 업데이트 로직 구현
    RiderProfile riderProfile = riderProfileRepository.findById(
            riderStatusRequestDto.riderProfileId())
        .orElseThrow(() -> new CustomException(ErrorCode.RIDER_NOT_FOUND));

    riderProfile.setToggleStatus(RiderToggleStatus.fromString(riderStatusRequestDto.riderStatus()));
  }

}
