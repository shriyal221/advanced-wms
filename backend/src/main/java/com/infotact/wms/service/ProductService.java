package com.infotact.wms.service;

import com.infotact.wms.api.dto.ProductRequest;
import com.infotact.wms.api.dto.ProductResponse;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.ProductCategory;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.ProductCategoryRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final WarehouseRepository warehouseRepository;

    public ProductService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        WarehouseRepository warehouseRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.warehouseRepository = warehouseRepository;
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
        return productRepository.findAllWithDetails()
            .stream()
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
}
