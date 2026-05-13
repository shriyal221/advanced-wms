package com.infotact.wms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.infotact.wms.api.dto.OrderCreateRequest;
import com.infotact.wms.api.dto.OrderLineRequest;
import com.infotact.wms.api.dto.OrderResponse;
import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.InsufficientStockException;
import com.infotact.wms.repository.InventoryItemRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    void packOrderRollsBackInventoryWhenStockIsInsufficient() {
        Warehouse warehouse = warehouseRepository.findByCode("BLR-01").orElseThrow();
        Product product = productRepository.save(new Product(
            "SKU-TEST-STOCK",
            "Stock Rollback Product",
            "SKU-TEST-STOCK",
            "Used by insufficient stock tests.",
            1,
            BigDecimal.ONE,
            BigDecimal.ONE,
            1.0,
            null,
            warehouse
        ));
        inventoryService.receive(new ReceiveStockRequest(product.getId(), 3, "ASN-ROLLBACK", "BATCH-ROLLBACK", null));
        Long warehouseId = warehouseRepository.findByCode("BLR-01").orElseThrow().getId();
        OrderResponse order = orderService.create(new OrderCreateRequest(warehouseId, null, List.of(
            new OrderLineRequest(product.getId(), 5)
        )));

        assertThatThrownBy(() -> orderService.pack(order.id()))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");

        assertThat(inventoryItemRepository.totalOnHand(product.getId())).isEqualTo(3);
    }
}
