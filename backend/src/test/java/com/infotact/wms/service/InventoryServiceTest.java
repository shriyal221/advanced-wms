package com.infotact.wms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.api.dto.ReceiveStockResponse;
import com.infotact.wms.domain.Product;
import com.infotact.wms.repository.InventoryItemRepository;
import com.infotact.wms.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Test
    void receiveShipmentAssignsStockToAvailableBin() {
        Product product = productRepository.save(new Product(
            "SKU-TEST-PUTAWAY",
            "Putaway Test Product",
            "Used by receiving flow tests.",
            2,
            BigDecimal.TEN
        ));

        ReceiveStockResponse response = inventoryService.receive(new ReceiveStockRequest(
            product.getId(),
            10,
            "ASN-TEST-001",
            "BATCH-TEST",
            null
        ));

        assertThat(response.receivedQuantity()).isEqualTo(10);
        assertThat(response.binCode()).isEqualTo("BIN-LKO-01");
        assertThat(inventoryItemRepository.totalOnHand(product.getId())).isEqualTo(10);
    }
}
