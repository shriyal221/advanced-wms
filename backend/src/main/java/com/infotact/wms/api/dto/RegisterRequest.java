package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 80) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "must start with a letter or number and contain only letters, numbers, dot, underscore, or hyphen") String username,
    @NotBlank @Size(min = 6, max = 120) String password,
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Email @Size(max = 160) String email,
    @Size(max = 40) @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "must be a valid phone number") String contactNumber,
    @NotBlank @Size(min = 2, max = 40) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "must contain only letters, numbers, dot, underscore, or hyphen") String warehouseCode,
    @NotBlank @Size(max = 160) String warehouseName,
    @Size(max = 255) String warehouseAddress
) {
}
