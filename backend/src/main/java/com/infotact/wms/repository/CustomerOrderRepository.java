package com.infotact.wms.repository;

import com.infotact.wms.domain.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @Query("""
        select distinct o
        from CustomerOrder o
        left join fetch o.warehouse
        left join fetch o.lines l
        left join fetch l.product
        where o.id = :id
        """)
    Optional<CustomerOrder> findWithLinesById(@Param("id") Long id);

    @Query("""
        select distinct o
        from CustomerOrder o
        left join fetch o.warehouse
        left join fetch o.lines l
        left join fetch l.product
        order by o.createdAt desc
        """)
    List<CustomerOrder> findAllWithLines();
}
