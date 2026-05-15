package com.infotact.wms.api.dto;

import com.infotact.wms.domain.InventoryTransaction;
import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String type,
    String productSku,
    String productName,
    String binCode,
    int quantityDelta,
    String reference,
    Instant createdAt
) {
    public static AuditLogResponse from(InventoryTransaction tx) {
        return new AuditLogResponse(
            tx.getId(),
            tx.getType().name(),
            tx.getProduct().getSku(),
            tx.getProduct().getName(),
            tx.getStorageBin().getCode(),
            tx.getQuantityDelta(),
            tx.getReference(),
            tx.getCreatedAt()
        );
    }
}
