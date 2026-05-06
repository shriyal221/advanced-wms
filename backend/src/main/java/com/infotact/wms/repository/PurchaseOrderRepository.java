package com.infotact.wms.repository;

import com.infotact.wms.domain.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("""
        select distinct p
        from PurchaseOrder p
        join fetch p.supplier
        join fetch p.warehouse
        left join fetch p.items i
        left join fetch i.product
        order by p.orderDate desc
        """)
    List<PurchaseOrder> findAllWithDetails();

    @Query("""
        select distinct p
        from PurchaseOrder p
        join fetch p.supplier
        join fetch p.warehouse
        left join fetch p.items i
        left join fetch i.product
        where p.id = :id
        """)
    Optional<PurchaseOrder> findWithDetailsById(@Param("id") Long id);
}
