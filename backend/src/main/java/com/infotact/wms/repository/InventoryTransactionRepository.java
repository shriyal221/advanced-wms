package com.infotact.wms.repository;

import com.infotact.wms.domain.InventoryTransaction;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

