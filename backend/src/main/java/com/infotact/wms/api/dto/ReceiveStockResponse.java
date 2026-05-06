package com.infotact.wms.api.dto;

public record ReceiveStockResponse(
    Long productId,
    String sku,
    Long binId,
    String binCode,
    int receivedQuantity,
    int binAvailableCapacity
) {
}
