package com.infotact.wms.api.dto;

public record PurchaseOrderLineResponse(
    Long productId,
    String sku,
    int quantity
) {
}
