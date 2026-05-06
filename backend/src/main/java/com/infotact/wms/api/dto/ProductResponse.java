package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Product;
import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String barcode,
    String description,
    int unitVolume,
    BigDecimal reorderThreshold,
    BigDecimal price,
    double weight,
    boolean active,
    Long categoryId,
    String categoryName,
    Long warehouseId,
    String warehouseCode
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getBarcode(),
            product.getDescription(),
            product.getUnitVolume(),
            product.getReorderThreshold(),
            product.getPrice(),
            product.getWeight(),
            product.isActive(),
            product.getCategory() == null ? null : product.getCategory().getId(),
            product.getCategory() == null ? null : product.getCategory().getName(),
            product.getWarehouse() == null ? null : product.getWarehouse().getId(),
            product.getWarehouse() == null ? null : product.getWarehouse().getCode()
        );
    }
}
