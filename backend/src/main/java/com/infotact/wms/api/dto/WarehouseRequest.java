package com.infotact.wms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
    @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "must contain only letters, numbers, dot, underscore, or hyphen") String code,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 255) String address
) {
}
