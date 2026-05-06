package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Warehouse;

public record WarehouseResponse(Long id, String code, String name, String address) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
            warehouse.getId(),
            warehouse.getCode(),
            warehouse.getName(),
            warehouse.getAddress()
        );
    }
}
