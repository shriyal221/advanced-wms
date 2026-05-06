package com.infotact.wms.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record PurchaseOrderRequest(
    @NotNull Long supplierId,
    @NotNull Long warehouseId,
    Instant expectedDate,
    @NotEmpty List<@Valid PurchaseOrderLineRequest> items
) {
}
