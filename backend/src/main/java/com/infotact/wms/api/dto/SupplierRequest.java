package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
    @NotBlank @Size(max = 160) String name,
    @Size(max = 255) String address,
    @Email @Size(max = 160) String contactEmail,
    @Size(max = 40) @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "must be a valid phone number") String phone
) {
}
