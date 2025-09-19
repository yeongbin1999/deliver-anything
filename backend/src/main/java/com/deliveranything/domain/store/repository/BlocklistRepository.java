package com.deliveranything.domain.store.repository;

import com.deliveranything.domain.store.entity.Blocklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlocklistRepository extends JpaRepository<Blocklist, Long> {

}
