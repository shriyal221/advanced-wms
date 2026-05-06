package com.infotact.wms.api;

import com.infotact.wms.api.dto.InventorySnapshotResponse;
import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.api.dto.ReceiveStockResponse;
import com.infotact.wms.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventorySnapshotResponse> snapshots() {
        return inventoryService.snapshots();
    }

    @PostMapping("/receive")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ReceiveStockResponse receive(@Valid @RequestBody ReceiveStockRequest request) {
        return inventoryService.receive(request);
    }
}
