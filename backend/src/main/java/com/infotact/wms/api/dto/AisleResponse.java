package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Aisle;

public record AisleResponse(Long id, String code, Long zoneId, String zoneCode) {
    public static AisleResponse from(Aisle aisle) {
        return new AisleResponse(
            aisle.getId(),
            aisle.getCode(),
            aisle.getZone().getId(),
            aisle.getZone().getCode()
        );
    }
}
