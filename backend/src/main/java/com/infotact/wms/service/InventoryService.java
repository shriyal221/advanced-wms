package com.infotact.wms.service;

import com.infotact.wms.api.dto.InventorySnapshotResponse;
import com.infotact.wms.api.dto.InventoryActionResponse;
import com.infotact.wms.api.dto.ReceiveStockRequest;
import com.infotact.wms.api.dto.ReceiveStockResponse;
import com.infotact.wms.api.dto.StockAdjustmentRequest;
import com.infotact.wms.api.dto.StockTransferRequest;
import com.infotact.wms.domain.CustomerOrderLine;
import com.infotact.wms.domain.InventoryItem;
import com.infotact.wms.domain.InventoryTransaction;
import com.infotact.wms.domain.InventoryTransactionType;
import com.infotact.wms.domain.Product;
import com.infotact.wms.domain.StorageBin;
import com.infotact.wms.exception.CapacityUnavailableException;
import com.infotact.wms.exception.InsufficientStockException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.InventoryItemRepository;
import com.infotact.wms.repository.InventoryTransactionRepository;
import com.infotact.wms.repository.StorageBinRepository;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.domain.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AppUserRepository appUserRepository;

    public InventoryService(
        ProductService productService,
        StorageBinRepository storageBinRepository,
        InventoryItemRepository inventoryItemRepository,
        InventoryTransactionRepository transactionRepository,
        AppUserRepository appUserRepository
    ) {
        this.productService = productService;
        this.storageBinRepository = storageBinRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.transactionRepository = transactionRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public ReceiveStockResponse receive(ReceiveStockRequest request) {
        Product product = productService.getProduct(request.productId());
        int requiredCapacity = product.getUnitVolume() * request.quantity();
        Long warehouseId = resolveWarehouseId(product);
        StorageBin bin = findPutawayBin(product, requiredCapacity, warehouseId);

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

    @Transactional
    public InventoryActionResponse adjust(StockAdjustmentRequest request) {
        if (request.quantityDelta() == 0) {
            throw new IllegalArgumentException("Quantity adjustment cannot be zero.");
        }

        Product product = productService.getProduct(request.productId());
        StorageBin bin = storageBinRepository.findByIdForUpdate(request.binId())
            .orElseThrow(() -> new ResourceNotFoundException("Storage bin not found: " + request.binId()));
        InventoryItem item = inventoryItemRepository
            .findByProductAndBinForUpdate(product.getId(), bin.getId())
            .orElseGet(() -> new InventoryItem(product, bin, 0));

        int capacityUnits = product.getUnitVolume() * Math.abs(request.quantityDelta());
        if (request.quantityDelta() > 0) {
            item.increase(request.quantityDelta());
            bin.reserveCapacity(capacityUnits);
        } else {
            int quantityToRemove = Math.abs(request.quantityDelta());
            item.decrease(quantityToRemove);
            bin.releaseCapacity(capacityUnits);
        }

        inventoryItemRepository.save(item);
        String reference = normalizeReference(request.reason(), "ADJUST");
        transactionRepository.save(new InventoryTransaction(
            InventoryTransactionType.ADJUST,
            product,
            bin,
            request.quantityDelta(),
            reference
        ));

        return new InventoryActionResponse(
            product.getId(),
            product.getSku(),
            bin.getId(),
            bin.getCode(),
            null,
            null,
            request.quantityDelta(),
            reference
        );
    }

    @Transactional
    public InventoryActionResponse transfer(StockTransferRequest request) {
        if (request.fromBinId().equals(request.toBinId())) {
            throw new IllegalArgumentException("Source and target bins must be different.");
        }

        Product product = productService.getProduct(request.productId());
        StorageBin fromBin = storageBinRepository.findByIdForUpdate(request.fromBinId())
            .orElseThrow(() -> new ResourceNotFoundException("Source storage bin not found: " + request.fromBinId()));
        StorageBin toBin = storageBinRepository.findByIdForUpdate(request.toBinId())
            .orElseThrow(() -> new ResourceNotFoundException("Target storage bin not found: " + request.toBinId()));

        if (!fromBin.getAisle().getZone().getWarehouse().getId().equals(toBin.getAisle().getZone().getWarehouse().getId())) {
            throw new IllegalStateException("Bin transfer must stay inside the same warehouse.");
        }

        InventoryItem sourceItem = inventoryItemRepository
            .findByProductAndBinForUpdate(product.getId(), fromBin.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No stock found for SKU " + product.getSku() + " in bin " + fromBin.getCode()));
        InventoryItem targetItem = inventoryItemRepository
            .findByProductAndBinForUpdate(product.getId(), toBin.getId())
            .orElseGet(() -> new InventoryItem(product, toBin, 0, sourceItem.getBatchNumber(), sourceItem.getExpiryDate()));

        int capacityUnits = product.getUnitVolume() * request.quantity();
        sourceItem.decrease(request.quantity());
        targetItem.increase(request.quantity());
        fromBin.releaseCapacity(capacityUnits);
        toBin.reserveCapacity(capacityUnits);

        inventoryItemRepository.save(sourceItem);
        inventoryItemRepository.save(targetItem);

        String reference = normalizeReference(request.reference(), "TRANSFER");
        transactionRepository.save(new InventoryTransaction(
            InventoryTransactionType.TRANSFER_OUT,
            product,
            fromBin,
            -request.quantity(),
            reference
        ));
        transactionRepository.save(new InventoryTransaction(
            InventoryTransactionType.TRANSFER_IN,
            product,
            toBin,
            request.quantity(),
            reference
        ));

        return new InventoryActionResponse(
            product.getId(),
            product.getSku(),
            fromBin.getId(),
            fromBin.getCode(),
            toBin.getId(),
            toBin.getCode(),
            request.quantity(),
            reference
        );
    }

    @Transactional(readOnly = true)
    public List<InventorySnapshotResponse> snapshots() {
        AppUser currentUser = getCurrentUser();
        return inventoryItemRepository.findAllWithDetails()
            .stream()
            .filter(item -> {
                if (currentUser != null && currentUser.getWarehouse() != null) {
                    return item.getStorageBin().getAisle().getZone().getWarehouse().getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
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

    private StorageBin findPutawayBin(Product product, int requiredCapacity, Long warehouseId) {
        if (product.getCategory() != null && product.getCategory().getPreferredZone() != null) {
            // Only use preferred zone if it belongs to the target warehouse
            if (warehouseId == null || product.getCategory().getPreferredZone().getWarehouse().getId().equals(warehouseId)) {
                List<StorageBin> preferredBins = storageBinRepository.findPutawayCandidatesInZoneForUpdate(
                    product.getCategory().getPreferredZone().getId(),
                    requiredCapacity,
                    PageRequest.of(0, 1)
                );
                if (!preferredBins.isEmpty()) {
                    return preferredBins.get(0);
                }
            }
        }

        if (warehouseId != null) {
            return storageBinRepository
                .findPutawayCandidatesForUpdate(warehouseId, requiredCapacity, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CapacityUnavailableException("No storage bin in warehouse has " + requiredCapacity + " free capacity units."));
        }

        return storageBinRepository
            .findPutawayCandidatesForUpdate(requiredCapacity, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .orElseThrow(() -> new CapacityUnavailableException("No storage bin has " + requiredCapacity + " free capacity units."));
    }

    private Long resolveWarehouseId(Product product) {
        AppUser currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getWarehouse() != null) {
            return currentUser.getWarehouse().getId();
        }
        if (product.getWarehouse() != null) {
            return product.getWarehouse().getId();
        }
        if (product.getCategory() != null && product.getCategory().getPreferredZone() != null) {
            return product.getCategory().getPreferredZone().getWarehouse().getId();
        }
        return null;
    }

    private String normalizeReference(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
