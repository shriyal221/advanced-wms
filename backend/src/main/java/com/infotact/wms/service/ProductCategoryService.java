package com.infotact.wms.service;

import com.infotact.wms.api.dto.ProductCategoryRequest;
import com.infotact.wms.api.dto.ProductCategoryResponse;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.domain.Zone;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.repository.ZoneRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCategoryService {
    private final ProductCategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;

    public ProductCategoryService(
        ProductCategoryRepository categoryRepository,
        WarehouseRepository warehouseRepository,
        ZoneRepository zoneRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndWarehouseId(name, warehouse.getId())) {
            throw new DuplicateResourceException("Category already exists in this warehouse: " + name);
        }
        ProductCategory parent = request.parentCategoryId() == null
            ? null
            : categoryRepository.findById(request.parentCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + request.parentCategoryId()));
        Zone preferredZone = request.preferredZoneId() == null
            ? null
            : zoneRepository.findById(request.preferredZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Preferred zone not found: " + request.preferredZoneId()));

        return ProductCategoryResponse.from(categoryRepository.save(new ProductCategory(name, warehouse, parent, preferredZone)));
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> list() {
        return categoryRepository.findAllWithDetails().stream().map(ProductCategoryResponse::from).toList();
    }
}
