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
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private Instant createdAt;

    private Instant updatedAt;

    private String name;

    @Column(unique = true)
    private String email;

    private String contactNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public AppUser(String username, String passwordHash, Role role, String name, String email, String contactNumber, Warehouse warehouse) {
        this(username, passwordHash, role);
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.warehouse = warehouse;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateProfile(String name, String email, String contactNumber, Role role, String status, Warehouse warehouse) {
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
        this.status = status;
        this.warehouse = warehouse;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }
}
