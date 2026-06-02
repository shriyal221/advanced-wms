package com.infotact.wms.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant expectedShipDate;

    private Instant packedAt;

    private Instant shippedAt;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderLine> lines = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String orderNumber, Warehouse warehouse, Instant expectedShipDate) {
        this.orderNumber = orderNumber;
        this.warehouse = warehouse;
        this.expectedShipDate = expectedShipDate;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public CustomerOrder(String orderNumber) {
        this(orderNumber, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpectedShipDate() {
        return expectedShipDate;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Instant getPackedAt() {
        return packedAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public List<CustomerOrderLine> getLines() {
        return lines;
    }

    public void addLine(CustomerOrderLine line) {
        lines.add(line);
        line.assignOrder(this);
    }

    public void startPicking() {
        ensureStatus(OrderStatus.PENDING);
        status = OrderStatus.PICKING;
    }

    public void markPacked() {
        if (status != OrderStatus.PENDING && status != OrderStatus.PICKING) {
            throw new IllegalStateException("Only pending or picking orders can be packed.");
        }
        status = OrderStatus.PACKED;
        packedAt = Instant.now();
    }

    public void markShipped() {
        ensureStatus(OrderStatus.PACKED);
        status = OrderStatus.SHIPPED;
        shippedAt = Instant.now();
    }

    private void ensureStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected order status " + expected + " but found " + status + ".");
        }
    }
}
