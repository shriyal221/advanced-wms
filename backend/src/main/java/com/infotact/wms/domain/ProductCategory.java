package com.infotact.wms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "product_categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "name"})
)
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Boolean active = true;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_zone_id")
    private Zone preferredZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ProductCategory parentCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    protected ProductCategory() {
    }

    public ProductCategory(String name, Warehouse warehouse, ProductCategory parentCategory, Zone preferredZone) {
        this.name = name;
        this.warehouse = warehouse;
        this.parentCategory = parentCategory;
        this.preferredZone = preferredZone;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public Zone getPreferredZone() {
        return preferredZone;
    }

    public ProductCategory getParentCategory() {
        return parentCategory;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }
}
