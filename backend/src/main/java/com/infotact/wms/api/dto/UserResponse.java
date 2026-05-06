package com.infotact.wms.api.dto;

import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Role;

public record UserResponse(
    Long id,
    String username,
    String name,
    String email,
    String contactNumber,
    Role role,
    String status,
    Long warehouseId,
    String warehouseCode
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getContactNumber(),
            user.getRole(),
            user.getStatus(),
            user.getWarehouse() == null ? null : user.getWarehouse().getId(),
            user.getWarehouse() == null ? null : user.getWarehouse().getCode()
        );
    }
}
