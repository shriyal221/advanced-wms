package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderLineRequest(
    @NotNull Long productId,
    @Min(1) int quantity
) {
}
