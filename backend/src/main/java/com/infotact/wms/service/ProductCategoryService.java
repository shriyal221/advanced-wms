package com.infotact.wms.service;

import com.infotact.wms.api.dto.ProductCategoryRequest;
import com.infotact.wms.api.dto.ProductCategoryResponse;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.domain.Zone;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.repository.ZoneRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCategoryService {
    private final ProductCategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final AppUserRepository appUserRepository;

    public ProductCategoryService(
        ProductCategoryRepository categoryRepository,
        WarehouseRepository warehouseRepository,
        ZoneRepository zoneRepository,
        AppUserRepository appUserRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
        this.appUserRepository = appUserRepository;
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
        AppUser currentUser = getCurrentUser();
        return categoryRepository.findAllWithDetails()
            .stream()
            .filter(category -> {
                if (currentUser != null && currentUser.getWarehouse() != null && category.getWarehouse() != null) {
                    return category.getWarehouse().getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(ProductCategoryResponse::from)
            .toList();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
