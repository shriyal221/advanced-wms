package com.infotact.wms.service;

import com.infotact.wms.api.dto.ProductRequest;
import com.infotact.wms.api.dto.ProductResponse;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final AppUserRepository appUserRepository;

    public ProductService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        WarehouseRepository warehouseRepository,
        AppUserRepository appUserRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("Product SKU already exists: " + sku);
        }
        String barcode = normalizeBarcode(request.barcode(), sku);
        if (productRepository.existsByBarcode(barcode)) {
            throw new DuplicateResourceException("Product barcode already exists: " + barcode);
        }

        ProductCategory category = request.categoryId() == null
            ? null
            : productCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Product category not found: " + request.categoryId()));
        Warehouse warehouse = request.warehouseId() == null
            ? null
            : warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));

        Product product = new Product(
            sku,
            request.name().trim(),
            barcode,
            request.description(),
            request.unitVolume(),
            request.reorderThreshold() == null ? BigDecimal.ZERO : request.reorderThreshold(),
            request.price() == null ? BigDecimal.ZERO : request.price(),
            request.weight() == null ? 0.0 : request.weight().doubleValue(),
            category,
            warehouse
        );
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        AppUser currentUser = getCurrentUser();
        return productRepository.findAllWithDetails()
            .stream()
            .filter(product -> {
                if (currentUser != null && currentUser.getWarehouse() != null && product.getWarehouse() != null) {
                    return product.getWarehouse().getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(ProductResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private String normalizeBarcode(String barcode, String fallbackSku) {
        if (barcode == null || barcode.isBlank()) {
            return fallbackSku;
        }
        return barcode.trim().toUpperCase();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
