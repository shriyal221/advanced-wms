package com.infotact.wms.repository;

import com.infotact.wms.domain.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByNameIgnoreCaseAndWarehouseId(String name, Long warehouseId);

    @Query("""
        select c
        from ProductCategory c
        join fetch c.warehouse
        left join fetch c.parentCategory
        left join fetch c.preferredZone
        order by c.name
        """)
    List<ProductCategory> findAllWithDetails();
}
