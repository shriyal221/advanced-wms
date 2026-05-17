package com.infotact.wms.api.dto;

public record InventoryActionResponse(
    Long productId,
    String sku,
    Long sourceBinId,
    String sourceBinCode,
    Long targetBinId,
    String targetBinCode,
    int quantityDelta,
    String reference
) {
}
