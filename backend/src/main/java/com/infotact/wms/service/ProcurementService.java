package com.infotact.wms.service;

import com.infotact.wms.api.dto.PurchaseOrderRequest;
import com.infotact.wms.api.dto.PurchaseOrderResponse;
import com.infotact.wms.api.dto.SupplierRequest;
import com.infotact.wms.api.dto.SupplierResponse;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.PurchaseOrder;
import com.infotact.wms.domain.PurchaseOrderItem;
import com.infotact.wms.domain.Supplier;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.PurchaseOrderRepository;
import com.infotact.wms.repository.SupplierRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementService {
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductService productService;

    public ProcurementService(
        SupplierRepository supplierRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        WarehouseRepository warehouseRepository,
        ProductService productService
    ) {
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.productService = productService;
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        String name = request.name().trim();
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Supplier already exists: " + name);
        }
        Supplier supplier = new Supplier(name, trimToNull(request.address()), trimToNull(request.contactEmail()), trimToNull(request.phone()));
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> listSuppliers() {
        return supplierRepository.findAll(Sort.by("name")).stream().map(SupplierResponse::from).toList();
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));
        PurchaseOrder purchaseOrder = new PurchaseOrder(supplier, warehouse, request.expectedDate());
        request.items().forEach(line -> {
            Product product = productService.getProduct(line.productId());
            purchaseOrder.addItem(new PurchaseOrderItem(product, line.quantity()));
        });
        return PurchaseOrderResponse.from(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> listPurchaseOrders() {
        return purchaseOrderRepository.findAllWithDetails().stream().map(PurchaseOrderResponse::from).toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
