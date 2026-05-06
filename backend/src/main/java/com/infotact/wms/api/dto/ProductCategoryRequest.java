package com.infotact.wms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
    @NotBlank @Size(max = 160) String name,
    @NotNull Long warehouseId,
    Long parentCategoryId,
    Long preferredZoneId
) {
}
