package com.infotact.wms.repository;

import com.infotact.wms.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    @Query("""
        select p
        from Product p
        left join fetch p.category
        left join fetch p.warehouse
        order by p.sku
        """)
    List<Product> findAllWithDetails();
}
