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
import jakarta.persistence.Version;

@Entity
@Table(name = "storage_bins")
public class StorageBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int usedCapacity;

    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    private BinStatus status = BinStatus.AVAILABLE;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aisle_id", nullable = false)
    private Aisle aisle;

    protected StorageBin() {
    }

    public StorageBin(String code, int capacity, Aisle aisle) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Bin capacity must be positive.");
        }
        this.code = code;
        this.capacity = capacity;
        this.aisle = aisle;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUsedCapacity() {
        return usedCapacity;
    }

    public int getAvailableCapacity() {
        return capacity - usedCapacity;
    }

    public Aisle getAisle() {
        return aisle;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public BinStatus getStatus() {
        return status;
    }

    public void reserveCapacity(int units) {
        if (units < 1) {
            throw new IllegalArgumentException("Units must be positive.");
        }
        if (usedCapacity + units > capacity) {
            throw new IllegalStateException("Bin capacity exceeded.");
        }
        usedCapacity += units;
        status = usedCapacity >= capacity ? BinStatus.FULL : BinStatus.AVAILABLE;
    }

    public void releaseCapacity(int units) {
        if (units < 1) {
            throw new IllegalArgumentException("Units must be positive.");
        }
        usedCapacity = Math.max(0, usedCapacity - units);
        status = isActive() ? BinStatus.AVAILABLE : BinStatus.INACTIVE;
    }
}
