package com.infotact.wms.api.dto;

import com.infotact.wms.domain.CustomerOrder;
import com.infotact.wms.domain.CustomerOrderLine;
import com.infotact.wms.domain.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    OrderStatus status,
    Instant createdAt,
    Instant expectedShipDate,
    Long warehouseId,
    String warehouseCode,
    List<OrderLineResponse> lines
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getExpectedShipDate(),
            order.getWarehouse() == null ? null : order.getWarehouse().getId(),
            order.getWarehouse() == null ? null : order.getWarehouse().getCode(),
            order.getLines().stream().map(OrderResponse::lineFrom).toList()
        );
    }

    private static OrderLineResponse lineFrom(CustomerOrderLine line) {
        return new OrderLineResponse(
            line.getProduct().getId(),
            line.getProduct().getSku(),
            line.getRequestedQuantity(),
            line.getPickedQuantity()
        );
    }
}
