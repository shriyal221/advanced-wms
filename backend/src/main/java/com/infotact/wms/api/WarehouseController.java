package com.infotact.wms.api;

import com.infotact.wms.api.dto.AisleRequest;
import com.infotact.wms.api.dto.AisleResponse;
import com.infotact.wms.api.dto.StorageBinRequest;
import com.infotact.wms.api.dto.StorageBinResponse;
import com.infotact.wms.api.dto.WarehouseRequest;
import com.infotact.wms.api.dto.WarehouseResponse;
import com.infotact.wms.api.dto.ZoneRequest;
import com.infotact.wms.api.dto.ZoneResponse;
import com.infotact.wms.service.WarehouseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/warehouses")
    public List<WarehouseResponse> warehouses() {
        return warehouseService.listWarehouses();
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasRole('ADMIN')")
    public WarehouseResponse createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return warehouseService.createWarehouse(request);
    }

    @PostMapping("/zones")
    @PreAuthorize("hasRole('ADMIN')")
    public ZoneResponse createZone(@Valid @RequestBody ZoneRequest request) {
        return warehouseService.createZone(request);
    }

    @GetMapping("/zones")
    public List<ZoneResponse> zones() {
        return warehouseService.listZones();
    }

    @PostMapping("/aisles")
    @PreAuthorize("hasRole('ADMIN')")
    public AisleResponse createAisle(@Valid @RequestBody AisleRequest request) {
        return warehouseService.createAisle(request);
    }

    @GetMapping("/aisles")
    public List<AisleResponse> aisles() {
        return warehouseService.listAisles();
    }

    @GetMapping("/bins")
    public List<StorageBinResponse> bins() {
        return warehouseService.listBins();
    }

    @PostMapping("/bins")
    @PreAuthorize("hasRole('ADMIN')")
    public StorageBinResponse createBin(@Valid @RequestBody StorageBinRequest request) {
        return warehouseService.createBin(request);
    }
}
