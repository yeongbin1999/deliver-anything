package com.deliveranything.domain.settlement.repository;

import com.deliveranything.domain.settlement.dto.SettlementProjection;
import com.deliveranything.domain.settlement.entity.SettlementBatch;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

  List<SettlementBatch> findAllByTargetId(Long targetId);

  List<SettlementBatch> findAllByTargetIdAndSettlementDateBetween(Long targetId, LocalDate start,
      LocalDate end);

  // 주간별 집계 쿼리
  @Query(value = """
          SELECT new com.deliveranything.domain.settlement.dto.SettlementProjection(
              SUM(s.targetTotalAmount),
              SUM(s.totalPlatformFee),
              SUM(s.settledAmount),
              SUM(s.transactionCount),
              MIN(s.settlementDate),
              MAX(s.settlementDate)
          )
          FROM SettlementBatch s
          WHERE s.targetId = :targetId
          GROUP BY FUNCTION('YEARWEEK', s.settlementDate)
          ORDER BY MIN(s.settlementDate) DESC
      """)
  List<SettlementProjection> findWeeklySettlementsByTargetId(@Param("targetId") Long targetId);


  // 월별 집계 쿼리
  @Query(value = """
          SELECT new com.deliveranything.domain.settlement.dto.SettlementProjection(
              SUM(s.targetTotalAmount),
              SUM(s.totalPlatformFee),
              SUM(s.settledAmount),
              SUM(s.transactionCount),
              MIN(s.settlementDate),
              MAX(s.settlementDate)
          )
          FROM SettlementBatch s
          WHERE s.targetId = :targetId
          GROUP BY FUNCTION('YEAR', s.settlementDate), FUNCTION('MONTH', s.settlementDate)
          ORDER BY MIN(s.settlementDate) DESC
      """)
  List<SettlementProjection> findMonthlySettlementsByTargetId(@Param("targetId") Long targetId);

  // 기간 정산
  @Query(value = """
          SELECT new com.deliveranything.domain.settlement.dto.SettlementProjection(
              SUM(s.targetTotalAmount),
              SUM(s.totalPlatformFee),
              SUM(s.settledAmount),
              SUM(s.transactionCount),
              MIN(s.settlementDate),
              MAX(s.settlementDate)
          )
          FROM SettlementBatch s
          WHERE s.targetId = :targetId
            AND s.settlementDate BETWEEN :startDate AND :endDate
      """)
  Optional<SettlementProjection> findSettlementByTargetIdAndPeriod(
      @Param("targetId") Long targetId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
