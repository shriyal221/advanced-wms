package com.infotact.wms.api.dto;

import com.infotact.wms.domain.ProductCategory;

public record ProductCategoryResponse(
    Long id,
    String name,
    boolean active,
    Long warehouseId,
    String warehouseCode,
    Long parentCategoryId,
    String parentCategoryName,
    Long preferredZoneId,
    String preferredZoneCode
) {
    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
            category.getId(),
            category.getName(),
            category.isActive(),
            category.getWarehouse().getId(),
            category.getWarehouse().getCode(),
            category.getParentCategory() == null ? null : category.getParentCategory().getId(),
            category.getParentCategory() == null ? null : category.getParentCategory().getName(),
            category.getPreferredZone() == null ? null : category.getPreferredZone().getId(),
            category.getPreferredZone() == null ? null : category.getPreferredZone().getCode()
        );
    }
}
