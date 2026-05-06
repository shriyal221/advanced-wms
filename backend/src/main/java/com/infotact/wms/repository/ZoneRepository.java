package com.infotact.wms.repository;

import com.infotact.wms.domain.Zone;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findFirstByCode(String code);
}
