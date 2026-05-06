package com.infotact.wms.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aisles")
public class Aisle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @OneToMany(mappedBy = "aisle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageBin> bins = new ArrayList<>();

    protected Aisle() {
    }

    public Aisle(String code, Zone zone) {
        this.code = code;
        this.zone = zone;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Zone getZone() {
        return zone;
    }

    public List<StorageBin> getBins() {
        return bins;
    }
}
