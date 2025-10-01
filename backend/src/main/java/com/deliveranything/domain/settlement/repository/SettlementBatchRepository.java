package com.deliveranything.domain.settlement.repository;

import com.deliveranything.domain.settlement.entity.SettlementBatch;
import com.deliveranything.domain.settlement.enums.TargetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

  List<SettlementBatch> findAllByTargetIdAndTargetType(Long targetId, TargetType targetType);
}
