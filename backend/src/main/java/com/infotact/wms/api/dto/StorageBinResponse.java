package com.infotact.wms.api.dto;

import com.infotact.wms.domain.StorageBin;

public record StorageBinResponse(
    Long id,
    String code,
    int capacity,
    int usedCapacity,
    int availableCapacity,
    String status,
    boolean active,
    String warehouseCode,
    String zoneCode,
    String aisleCode
) {
    public static StorageBinResponse from(StorageBin bin) {
        return new StorageBinResponse(
            bin.getId(),
            bin.getCode(),
            bin.getCapacity(),
            bin.getUsedCapacity(),
            bin.getAvailableCapacity(),
            bin.getStatus() == null ? null : bin.getStatus().name(),
            bin.isActive(),
            bin.getAisle().getZone().getWarehouse().getCode(),
            bin.getAisle().getZone().getCode(),
            bin.getAisle().getCode()
        );
    }
}
