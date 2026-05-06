package com.infotact.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_sku", columnList = "sku"),
        @Index(name = "idx_products_barcode", columnList = "barcode")
    }
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String barcode;

    private String description;

    @Column(nullable = false)
    private int unitVolume;

    @Column(name = "min_threshold", nullable = false)
    private BigDecimal reorderThreshold;

    private BigDecimal price = BigDecimal.ZERO;

    private Double weight;

    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    protected Product() {
    }

    public Product(String sku, String name, String description, int unitVolume, BigDecimal reorderThreshold) {
        this.sku = sku;
        this.name = name;
        this.barcode = sku;
        this.description = description;
        this.unitVolume = unitVolume;
        this.reorderThreshold = reorderThreshold;
    }

    public Product(
        String sku,
        String name,
        String barcode,
        String description,
        int unitVolume,
        BigDecimal reorderThreshold,
        BigDecimal price,
        double weight,
        ProductCategory category,
        Warehouse warehouse
    ) {
        this.sku = sku;
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.unitVolume = unitVolume;
        this.reorderThreshold = reorderThreshold;
        this.price = price;
        this.weight = weight;
        this.category = category;
        this.warehouse = warehouse;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public int getUnitVolume() {
        return unitVolume;
    }

    public BigDecimal getReorderThreshold() {
        return reorderThreshold;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public double getWeight() {
        return weight == null ? 0.0 : weight;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }
}
