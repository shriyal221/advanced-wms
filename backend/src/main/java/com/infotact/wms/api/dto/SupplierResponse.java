package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Supplier;

public record SupplierResponse(
    Long id,
    String name,
    String address,
    String contactEmail,
    String phone,
    boolean active
) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
            supplier.getId(),
            supplier.getName(),
            supplier.getAddress(),
            supplier.getContactEmail(),
            supplier.getPhone(),
            supplier.isActive()
        );
    }
}
