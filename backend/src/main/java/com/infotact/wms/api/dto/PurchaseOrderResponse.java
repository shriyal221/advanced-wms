package com.infotact.wms.api.dto;

import com.infotact.wms.domain.PurchaseOrder;
import com.infotact.wms.domain.PurchaseOrderItem;
import com.infotact.wms.domain.PurchaseOrderStatus;
import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
    Long id,
    Instant orderDate,
    Instant expectedDate,
    PurchaseOrderStatus status,
    Long supplierId,
    String supplierName,
    Long warehouseId,
    String warehouseCode,
    List<PurchaseOrderLineResponse> items
) {
    public static PurchaseOrderResponse from(PurchaseOrder purchaseOrder) {
        return new PurchaseOrderResponse(
            purchaseOrder.getId(),
            purchaseOrder.getOrderDate(),
            purchaseOrder.getExpectedDate(),
            purchaseOrder.getStatus(),
            purchaseOrder.getSupplier().getId(),
            purchaseOrder.getSupplier().getName(),
            purchaseOrder.getWarehouse().getId(),
            purchaseOrder.getWarehouse().getCode(),
            purchaseOrder.getItems().stream().map(PurchaseOrderResponse::lineFrom).toList()
        );
    }

    private static PurchaseOrderLineResponse lineFrom(PurchaseOrderItem item) {
        return new PurchaseOrderLineResponse(
            item.getProduct().getId(),
            item.getProduct().getSku(),
            item.getQuantity()
        );
    }
}
