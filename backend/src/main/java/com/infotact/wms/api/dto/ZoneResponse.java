package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Zone;

public record ZoneResponse(Long id, String code, String name, Long warehouseId, String warehouseCode) {
    public static ZoneResponse from(Zone zone) {
        return new ZoneResponse(
            zone.getId(),
            zone.getCode(),
            zone.getName(),
            zone.getWarehouse().getId(),
            zone.getWarehouse().getCode()
        );
    }
}
