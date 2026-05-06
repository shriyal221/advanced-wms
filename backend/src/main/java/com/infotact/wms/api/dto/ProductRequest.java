package com.infotact.wms.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 80) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "must be alphanumeric with optional dot, underscore, or hyphen") String sku,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 255) String barcode,
    @Size(max = 255) String description,
    @Min(1) int unitVolume,
    @DecimalMin("0.0") BigDecimal reorderThreshold,
    @DecimalMin("0.0") BigDecimal price,
    @DecimalMin("0.0") BigDecimal weight,
    Long categoryId,
    Long warehouseId
) {
}
