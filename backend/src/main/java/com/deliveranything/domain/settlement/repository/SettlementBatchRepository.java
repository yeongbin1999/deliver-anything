package com.deliveranything.domain.settlement.repository;

import com.deliveranything.domain.settlement.entity.SettlementBatch;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

  List<SettlementBatch> findAllByTargetId(Long targetId);

  List<SettlementBatch> findAllByTargetIdAndSettlementDateBetween(Long targetId, LocalDate start,
      LocalDate end);
}
