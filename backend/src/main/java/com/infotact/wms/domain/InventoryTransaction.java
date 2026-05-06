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
import java.time.Instant;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryTransactionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_bin_id", nullable = false)
    private StorageBin storageBin;

    @Column(nullable = false)
    private int quantityDelta;

    private String reference;

    @Column(nullable = false)
    private Instant createdAt;

    protected InventoryTransaction() {
    }

    public InventoryTransaction(InventoryTransactionType type, Product product, StorageBin storageBin, int quantityDelta, String reference) {
        this.type = type;
        this.product = product;
        this.storageBin = storageBin;
        this.quantityDelta = quantityDelta;
        this.reference = reference;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
}
