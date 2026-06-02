package com.infotact.wms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.infotact.wms.api.dto.InventoryActionResponse;
import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.api.dto.ReceiveStockResponse;
import com.infotact.wms.api.dto.StockAdjustmentRequest;
import com.infotact.wms.api.dto.StockTransferRequest;
import com.infotact.wms.domain.InventoryItem;
import com.infotact.wms.domain.InventoryTransactionType;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.repository.InventoryItemRepository;
import com.infotact.wms.repository.InventoryTransactionRepository;
import com.infotact.wms.repository.ProductRepository;
import com.infotact.wms.repository.StorageBinRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class InventoryServiceTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private StorageBinRepository storageBinRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    void receiveShipmentAssignsStockToAvailableBin() {
        Warehouse warehouse = warehouseRepository.findByCode("LKO-01").orElseThrow();
        Product product = productRepository.save(new Product(
            "SKU-TEST-PUTAWAY",
            "Putaway Test Product",
            "SKU-TEST-PUTAWAY",
            "Used by receiving flow tests.",
            2,
            BigDecimal.TEN,
            BigDecimal.TEN,
            1.0,
            null,
            warehouse
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

    @Test
    void stockAdjustmentUpdatesInventoryAndBinCapacity() {
        Warehouse warehouse = warehouseRepository.findByCode("LKO-01").orElseThrow();
        StorageBin bin = storageBinRepository.findAllWithLocation()
            .stream()
            .filter(candidate -> candidate.getCode().equals("BIN-LKO-01"))
            .findFirst()
            .orElseThrow();
        int initialUsedCapacity = bin.getUsedCapacity();
        Product product = productRepository.save(new Product(
            "SKU-TEST-ADJUST",
            "Adjustment Test Product",
            "SKU-TEST-ADJUST",
            "Used by adjustment flow tests.",
            1,
            BigDecimal.TEN,
            BigDecimal.TEN,
            1.0,
            null,
            warehouse
        ));

        InventoryActionResponse increase = inventoryService.adjust(new StockAdjustmentRequest(
            product.getId(),
            bin.getId(),
            8,
            "Cycle count increase"
        ));
        inventoryService.adjust(new StockAdjustmentRequest(
            product.getId(),
            bin.getId(),
            -3,
            "Cycle count correction"
        ));

        StorageBin updatedBin = storageBinRepository.findById(bin.getId()).orElseThrow();

        assertThat(increase.quantityDelta()).isEqualTo(8);
        assertThat(inventoryItemRepository.totalOnHand(product.getId())).isEqualTo(5);
        assertThat(updatedBin.getUsedCapacity()).isEqualTo(initialUsedCapacity + 5);
        assertThat(inventoryTransactionRepository.findAll())
            .extracting(transaction -> transaction.getType())
            .contains(InventoryTransactionType.ADJUST);
    }

    @Test
    void stockTransferMovesQuantityBetweenBinsInSameWarehouse() {
        Warehouse warehouse = warehouseRepository.findByCode("BLR-01").orElseThrow();
        Product product = productRepository.save(new Product(
            "SKU-TEST-TRANSFER",
            "Transfer Test Product",
            "SKU-TEST-TRANSFER",
            "Used by bin transfer flow tests.",
            1,
            BigDecimal.TEN,
            BigDecimal.TEN,
            1.0,
            null,
            warehouse
        ));
        ReceiveStockResponse received = inventoryService.receive(new ReceiveStockRequest(
            product.getId(),
            12,
            "ASN-TRANSFER-001",
            "BATCH-TRANSFER",
            null
        ));
        StorageBin targetBin = storageBinRepository.findAllWithLocation()
            .stream()
            .filter(candidate -> candidate.getAisle().getZone().getWarehouse().getId().equals(warehouse.getId()))
            .filter(candidate -> !candidate.getId().equals(received.binId()))
            .findFirst()
            .orElseThrow();
        int sourceUsedAfterReceive = storageBinRepository.findById(received.binId()).orElseThrow().getUsedCapacity();
        int targetUsedBeforeTransfer = targetBin.getUsedCapacity();

        InventoryActionResponse response = inventoryService.transfer(new StockTransferRequest(
            product.getId(),
            received.binId(),
            targetBin.getId(),
            5,
            "MOVE-TRANSFER-TEST"
        ));

        StorageBin sourceBin = storageBinRepository.findById(received.binId()).orElseThrow();
        StorageBin updatedTargetBin = storageBinRepository.findById(targetBin.getId()).orElseThrow();
        InventoryItem sourceItem = inventoryItemRepository.findAllWithDetails()
            .stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .filter(item -> item.getStorageBin().getId().equals(received.binId()))
            .findFirst()
            .orElseThrow();
        InventoryItem targetItem = inventoryItemRepository.findAllWithDetails()
            .stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .filter(item -> item.getStorageBin().getId().equals(targetBin.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(response.quantityDelta()).isEqualTo(5);
        assertThat(inventoryItemRepository.totalOnHand(product.getId())).isEqualTo(12);
        assertThat(sourceItem.getQuantity()).isEqualTo(7);
        assertThat(targetItem.getQuantity()).isEqualTo(5);
        assertThat(sourceBin.getUsedCapacity()).isEqualTo(sourceUsedAfterReceive - 5);
        assertThat(updatedTargetBin.getUsedCapacity()).isEqualTo(targetUsedBeforeTransfer + 5);
        assertThat(inventoryTransactionRepository.findAll())
            .extracting(transaction -> transaction.getType())
            .contains(InventoryTransactionType.TRANSFER_OUT, InventoryTransactionType.TRANSFER_IN);
    }
}
