package com.infotact.wms.repository;

import com.infotact.wms.domain.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCode(String code);

    Optional<Warehouse> findByCode(String code);
}
