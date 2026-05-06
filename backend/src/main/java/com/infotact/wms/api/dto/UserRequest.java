package com.infotact.wms.api.dto;

import com.infotact.wms.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(max = 80) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "must be alphanumeric with optional dot, underscore, or hyphen") String username,
    @Size(min = 6, max = 120) String password,
    @NotNull Role role,
    @NotBlank @Size(max = 160) String name,
    @Email @Size(max = 160) String email,
    @Size(max = 40) @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "must be a valid phone number") String contactNumber,
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "must be ACTIVE or INACTIVE") String status,
    Long warehouseId
) {
}
