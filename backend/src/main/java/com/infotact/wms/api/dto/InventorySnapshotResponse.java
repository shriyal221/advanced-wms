package com.infotact.wms.api.dto;

public record InventorySnapshotResponse(
    Long productId,
    String sku,
    String productName,
    Long binId,
    String binCode,
    String warehouseCode,
    int quantity,
    int reservedQuantity,
    int availableQuantity,
    String status
) {
}
