package com.infotact.wms.service;

import com.infotact.wms.api.dto.InventorySnapshotResponse;
import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.api.dto.ReceiveStockResponse;
import com.infotact.wms.domain.CustomerOrderLine;
import com.infotact.wms.domain.InventoryItem;
import com.infotact.wms.domain.InventoryTransaction;
import com.infotact.wms.domain.InventoryTransactionType;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.exception.CapacityUnavailableException;
import com.infotact.wms.exception.InsufficientStockException;
import com.infotact.wms.repository.InventoryItemRepository;
import com.infotact.wms.repository.InventoryTransactionRepository;
import com.infotact.wms.repository.StorageBinRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final ProductService productService;
    private final StorageBinRepository storageBinRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(
        ProductService productService,
        StorageBinRepository storageBinRepository,
        InventoryItemRepository inventoryItemRepository,
        InventoryTransactionRepository transactionRepository
    ) {
        this.productService = productService;
        this.storageBinRepository = storageBinRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public ReceiveStockResponse receive(ReceiveStockRequest request) {
        Product product = productService.getProduct(request.productId());
        int requiredCapacity = product.getUnitVolume() * request.quantity();
        StorageBin bin = findPutawayBin(product, requiredCapacity);

        InventoryItem item = inventoryItemRepository
            .findByProductAndBinForUpdate(product.getId(), bin.getId())
            .orElseGet(() -> new InventoryItem(product, bin, 0, request.batchNumber(), request.expiryDate()));

        item.increase(request.quantity());
        bin.reserveCapacity(requiredCapacity);
        inventoryItemRepository.save(item);
        transactionRepository.save(new InventoryTransaction(
            InventoryTransactionType.RECEIVE,
            product,
            bin,
            request.quantity(),
            request.reference()
        ));

        return new ReceiveStockResponse(
            product.getId(),
            product.getSku(),
            bin.getId(),
            bin.getCode(),
            request.quantity(),
            bin.getAvailableCapacity()
        );
    }

    @Transactional(readOnly = true)
    public List<InventorySnapshotResponse> snapshots() {
        return inventoryItemRepository.findAllWithDetails()
            .stream()
            .map(item -> new InventorySnapshotResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getStorageBin().getId(),
                item.getStorageBin().getCode(),
                item.getStorageBin().getAisle().getZone().getWarehouse().getCode(),
                item.getQuantity(),
                item.getReservedQuantity(),
                item.getAvailableQuantity(),
                item.getStatus() == null ? null : item.getStatus().name()
            ))
            .toList();
    }

    @Transactional
    public void pickForOrderLine(CustomerOrderLine line, String reference) {
        int remaining = line.getRequestedQuantity();
        List<InventoryItem> availableItems = inventoryItemRepository.findAvailableForProductForUpdate(line.getProduct().getId());

        for (InventoryItem item : availableItems) {
            if (remaining == 0) {
                break;
            }
            int removed = item.decreaseUpTo(remaining);
            if (removed > 0) {
                item.getStorageBin().releaseCapacity(removed * line.getProduct().getUnitVolume());
                transactionRepository.save(new InventoryTransaction(
                    InventoryTransactionType.PICK,
                    line.getProduct(),
                    item.getStorageBin(),
                    -removed,
                    reference
                ));
                remaining -= removed;
            }
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                "Insufficient stock for SKU " + line.getProduct().getSku() + ". Missing quantity: " + remaining
            );
        }

        line.markPicked(line.getRequestedQuantity());
    }

    private StorageBin findPutawayBin(Product product, int requiredCapacity) {
        if (product.getCategory() != null && product.getCategory().getPreferredZone() != null) {
            List<StorageBin> preferredBins = storageBinRepository.findPutawayCandidatesInZoneForUpdate(
                product.getCategory().getPreferredZone().getId(),
                requiredCapacity,
                PageRequest.of(0, 1)
            );
            if (!preferredBins.isEmpty()) {
                return preferredBins.get(0);
            }
        }

        return storageBinRepository
            .findPutawayCandidatesForUpdate(requiredCapacity, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .orElseThrow(() -> new CapacityUnavailableException("No storage bin has " + requiredCapacity + " free capacity units."));
    }
}
