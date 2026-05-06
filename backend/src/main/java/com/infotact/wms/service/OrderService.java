package com.infotact.wms.service;

import com.infotact.wms.api.dto.OrderCreateRequest;
import com.infotact.wms.api.dto.OrderLineRequest;
import com.infotact.wms.api.dto.OrderResponse;
import com.infotact.wms.domain.CustomerOrder;
import com.infotact.wms.domain.CustomerOrderLine;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.CustomerOrderRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final WarehouseRepository warehouseRepository;

    public OrderService(
        CustomerOrderRepository customerOrderRepository,
        ProductService productService,
        InventoryService inventoryService,
        WarehouseRepository warehouseRepository
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));
        CustomerOrder order = new CustomerOrder(
            "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            warehouse,
            request.expectedShipDate()
        );
        for (OrderLineRequest lineRequest : request.lines()) {
            Product product = productService.getProduct(lineRequest.productId());
            order.addLine(new CustomerOrderLine(product, lineRequest.quantity()));
        }
        return OrderResponse.from(customerOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return customerOrderRepository.findAllWithLines()
            .stream()
            .map(OrderResponse::from)
            .toList();
    }

    @Transactional
    public OrderResponse startPicking(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        order.startPicking();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse pack(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        String reference = "PACK:" + order.getOrderNumber();
        for (CustomerOrderLine line : order.getLines()) {
            inventoryService.pickForOrderLine(line, reference);
        }
        order.markPacked();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse ship(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        order.markShipped();
        return OrderResponse.from(order);
    }

    private CustomerOrder getOrder(Long orderId) {
        return customerOrderRepository.findWithLinesById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
