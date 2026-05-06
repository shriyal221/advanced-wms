package com.infotact.wms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AisleRequest(
    @NotNull Long zoneId,
    @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "must contain only letters, numbers, dot, underscore, or hyphen") String code
) {
}
