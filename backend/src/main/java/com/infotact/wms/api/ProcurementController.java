package com.infotact.wms.api;

import com.infotact.wms.api.dto.PurchaseOrderRequest;
import com.infotact.wms.api.dto.PurchaseOrderResponse;
import com.infotact.wms.api.dto.SupplierRequest;
import com.infotact.wms.api.dto.SupplierResponse;
import com.infotact.wms.service.ProcurementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {
    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @GetMapping("/suppliers")
    public List<SupplierResponse> suppliers() {
        return procurementService.listSuppliers();
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        return procurementService.createSupplier(request);
    }

    @GetMapping("/purchase-orders")
    public List<PurchaseOrderResponse> purchaseOrders() {
        return procurementService.listPurchaseOrders();
    }

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public PurchaseOrderResponse createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return procurementService.createPurchaseOrder(request);
    }
}
