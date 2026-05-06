package com.infotact.wms.repository;

import com.infotact.wms.domain.StorageBin;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageBinRepository extends JpaRepository<StorageBin, Long> {
    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b
        from StorageBin b
        join fetch b.aisle a
        join fetch a.zone z
        join fetch z.warehouse
        where b.capacity - b.usedCapacity >= :requiredCapacity
        order by (b.capacity - b.usedCapacity) desc
        """)
    List<StorageBin> findPutawayCandidatesForUpdate(
        @Param("requiredCapacity") int requiredCapacity,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b
        from StorageBin b
        join fetch b.aisle a
        join fetch a.zone z
        join fetch z.warehouse
        where z.id = :zoneId and b.capacity - b.usedCapacity >= :requiredCapacity
        order by (b.capacity - b.usedCapacity) desc
        """)
    List<StorageBin> findPutawayCandidatesInZoneForUpdate(
        @Param("zoneId") Long zoneId,
        @Param("requiredCapacity") int requiredCapacity,
        Pageable pageable
    );

    @Query("""
        select b
        from StorageBin b
        join fetch b.aisle a
        join fetch a.zone z
        join fetch z.warehouse
        order by b.code
        """)
    List<StorageBin> findAllWithLocation();
}
