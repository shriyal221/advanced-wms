package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReceiveStockRequest(
    @NotNull Long productId,
    @Min(1) int quantity,
    @Size(max = 120) String reference,
    @Size(max = 80) String batchNumber,
    LocalDate expiryDate
) {
}
