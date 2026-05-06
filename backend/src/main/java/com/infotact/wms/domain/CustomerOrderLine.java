package com.infotact.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_order_lines")
public class CustomerOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int requestedQuantity;

    @Column(nullable = false)
    private int pickedQuantity;

    protected CustomerOrderLine() {
    }

    public CustomerOrderLine(Product product, int requestedQuantity) {
        this.product = product;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getId() {
        return id;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public Product getProduct() {
        return product;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getPickedQuantity() {
        return pickedQuantity;
    }

    void assignOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public void markPicked(int quantity) {
        this.pickedQuantity = quantity;
    }
}
