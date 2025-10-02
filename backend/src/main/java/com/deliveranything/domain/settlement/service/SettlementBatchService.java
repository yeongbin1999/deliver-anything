package com.deliveranything.domain.settlement.service;

import com.deliveranything.domain.settlement.dto.SettlementResponse;
import com.deliveranything.domain.settlement.entity.SettlementBatch;
import com.deliveranything.domain.settlement.entity.SettlementDetail;
import com.deliveranything.domain.settlement.enums.SettlementStatus;
import com.deliveranything.domain.settlement.repository.SettlementBatchRepository;
import com.deliveranything.domain.settlement.repository.SettlementDetailRepository;
import com.deliveranything.domain.settlement.service.dto.SettlementSummary;
import com.deliveranything.global.exception.CustomException;
import com.deliveranything.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SettlementBatchService {

  private final SettlementDetailRepository settlementDetailRepository;
  private final SettlementBatchRepository settlementBatchRepository;

  @Transactional(readOnly = true)
  public List<SettlementResponse> getSettlementsByDay(Long targetId) {
    return settlementBatchRepository.findAllByTargetId(targetId).stream()
        .map(SettlementResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SettlementResponse> getSettlementsByWeek(Long targetId) {
    return settlementBatchRepository.findWeeklySettlementsByTargetId(targetId).stream()
        .map(SettlementResponse::fromProjection)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SettlementResponse> getSettlementsByMonth(Long targetId) {
    return settlementBatchRepository.findMonthlySettlementsByTargetId(targetId).stream()
        .map(SettlementResponse::fromProjection)
        .toList();
  }

  @Transactional(readOnly = true)
  public SettlementResponse getSettlementByPeriod(
      Long targetId,
      LocalDate startDate,
      LocalDate endDate
  ) {
    return SettlementResponse.fromProjection(
        settlementBatchRepository.findSettlementByTargetIdAndPeriod(targetId, startDate, endDate)
            .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND)));
  }

  // "초 분 시 일 월 요일"
  // "0 0 0 * * *" 매일 0시 0분 0초에 실행
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void processDailySettlements() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    List<SettlementDetail> settlementDetails = settlementDetailRepository.findAllByStatusAndDateTime(
        SettlementStatus.PENDING, yesterday.atStartOfDay(), LocalDate.now().atStartOfDay());

    if (settlementDetails.isEmpty()) {
      return;
    }

    // 데이터 집계: 판매자/라이더 회원 별로 그룹핑하여 금액 합산
    Map<Long, SettlementSummary> summaryMap = settlementDetails.stream()
        .collect(Collectors.groupingBy(
            SettlementDetail::getTargetId,
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> new SettlementSummary(
                    list.stream().map(SettlementDetail::getTargetAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                    list.stream().map(SettlementDetail::getPlatformFee)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                    list.size()
                )
            )
        ));

    // 그룹 정산 생성 및 각 정산 대상 상태 업데이트
    summaryMap.forEach((targetId, summary) -> {
      SettlementBatch batch = SettlementBatch.builder()
          .settlementDate(yesterday)
          .targetId(targetId)
          .targetTotalAmount(summary.totalTargetAmount())
          .totalPlatformFee(summary.totalPlatformFee())
          .settledAmount(summary.totalTargetAmount().subtract(summary.totalPlatformFee()))
          .transactionCount(summary.transactionCount())
          .build();

      SettlementBatch savedBatch = settlementBatchRepository.save(batch);

      settlementDetails.stream()
          .filter(d -> d.getTargetId().equals(targetId))
          .forEach(detail -> detail.process(savedBatch.getId()));
    });
  }

  // 이번 주 월요일 ~ 어제까지의 정산 목록
  public List<SettlementResponse> getThisWeekSettlementBatches(Long targetId) {
    LocalDate today = LocalDate.now();
    return settlementBatchRepository.findAllByTargetIdAndSettlementDateBetween(targetId,
            today.with(DayOfWeek.MONDAY), today.minusDays(1))
        .stream()
        .map(SettlementResponse::from)
        .toList();
  }

  // 이번 달 1일 ~ 어제까지의 정산 목록
  public List<SettlementResponse> getThisMonthSettlementBatches(Long targetId) {
    LocalDate today = LocalDate.now();
    return settlementBatchRepository.findAllByTargetIdAndSettlementDateBetween(targetId,
            today.withDayOfMonth(1), today.minusDays(1))
        .stream()
        .map(SettlementResponse::from)
        .toList();
  }
}
