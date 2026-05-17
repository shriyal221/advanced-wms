package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockTransferRequest(
    @NotNull Long productId,
    @NotNull Long fromBinId,
    @NotNull Long toBinId,
    @Min(1) int quantity,
    @Size(max = 120) String reference
) {
}
