package com.infotact.wms.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant orderDate;

    private Instant expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public PurchaseOrder(Supplier supplier, Warehouse warehouse, Instant expectedDate) {
        this.supplier = supplier;
        this.warehouse = warehouse;
        this.expectedDate = expectedDate;
        this.orderDate = Instant.now();
        this.status = PurchaseOrderStatus.ORDERED;
    }

    public Long getId() {
        return id;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public Instant getExpectedDate() {
        return expectedDate;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.assignPurchaseOrder(this);
    }

    public void markReceived() {
        if (status == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled purchase orders cannot be received.");
        }
        status = PurchaseOrderStatus.RECEIVED;
    }
}
