package com.infotact.wms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StorageBinRequest(
    @NotNull Long aisleId,
    @NotBlank @Size(max = 60) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "must contain only letters, numbers, dot, underscore, or hyphen") String code,
    @Min(1) int capacity
) {
}
