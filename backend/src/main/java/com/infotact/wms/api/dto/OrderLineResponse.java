package com.infotact.wms.api.dto;

public record OrderLineResponse(
    Long productId,
    String sku,
    int requestedQuantity,
    int pickedQuantity
) {
}
