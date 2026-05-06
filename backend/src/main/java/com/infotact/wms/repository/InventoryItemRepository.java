package com.infotact.wms.repository;

import com.infotact.wms.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from InventoryItem i
        join fetch i.product
        join fetch i.storageBin b
        where i.product.id = :productId and b.id = :binId
        """)
    Optional<InventoryItem> findByProductAndBinForUpdate(@Param("productId") Long productId, @Param("binId") Long binId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from InventoryItem i
        join fetch i.product p
        join fetch i.storageBin b
        where p.id = :productId and i.quantity > 0
        order by i.quantity desc
        """)
    List<InventoryItem> findAvailableForProductForUpdate(@Param("productId") Long productId);

    @Query("""
        select i
        from InventoryItem i
        join fetch i.product p
        join fetch i.storageBin b
        join fetch b.aisle a
        join fetch a.zone z
        join fetch z.warehouse
        order by p.sku, b.code
        """)
    List<InventoryItem> findAllWithDetails();

    @Query("select coalesce(sum(i.quantity), 0) from InventoryItem i where i.product.id = :productId")
    long totalOnHand(@Param("productId") Long productId);
}
