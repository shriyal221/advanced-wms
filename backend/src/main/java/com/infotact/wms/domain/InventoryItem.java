package com.infotact.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "inventory_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "storage_bin_id"})
)
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_bin_id", nullable = false)
    private StorageBin storageBin;

    @Column(nullable = false)
    private int quantity;

    private Integer reservedQuantity;

    private String batchNumber;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    private Instant createdAt;

    private Instant updatedAt;

    @Version
    private long version;

    protected InventoryItem() {
    }

    public InventoryItem(Product product, StorageBin storageBin, int quantity) {
        this.product = product;
        this.storageBin = storageBin;
        this.quantity = quantity;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public InventoryItem(Product product, StorageBin storageBin, int quantity, String batchNumber, LocalDate expiryDate) {
        this(product, storageBin, quantity);
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public StorageBin getStorageBin() {
        return storageBin;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity == null ? 0 : reservedQuantity;
    }

    public int getAvailableQuantity() {
        return quantity - getReservedQuantity();
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void increase(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        quantity += amount;
        updatedAt = Instant.now();
    }

    public int decreaseUpTo(int requested) {
        if (requested < 1) {
            throw new IllegalArgumentException("Requested amount must be positive.");
        }
        int removed = Math.min(getAvailableQuantity(), requested);
        quantity -= removed;
        updatedAt = Instant.now();
        return removed;
    }

    public void decrease(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException("Not enough available inventory.");
        }
        quantity -= amount;
        updatedAt = Instant.now();
    }
}
