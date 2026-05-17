package com.infotact.wms.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
    @NotNull Long productId,
    @NotNull Long binId,
    int quantityDelta,
    @Size(max = 120) String reason
) {
}
