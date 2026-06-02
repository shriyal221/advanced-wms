package com.infotact.wms.service;

import com.infotact.wms.api.dto.UserRequest;
import com.infotact.wms.api.dto.UserResponse;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Role;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.exception.ResourceNotFoundException;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.WarehouseRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class UserService {
    private final AppUserRepository appUserRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, WarehouseRepository warehouseRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.warehouseRepository = warehouseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        AppUser currentUser = getCurrentUser();
        return appUserRepository.findAll()
            .stream()
            .filter(user -> {
                if (currentUser != null && currentUser.getWarehouse() != null) {
                    return user.getWarehouse() != null
                        && user.getWarehouse().getId().equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(UserResponse::from)
            .toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        String username = request.username().trim().toLowerCase();
        if (appUserRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User already exists: " + username);
        }
        validateUniqueEmail(request.email(), null);
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required for a new user.");
        }
        Warehouse warehouse = resolveWarehouse(request.warehouseId());
        AppUser user = new AppUser(
            username,
            passwordEncoder.encode(request.password()),
            request.role(),
            request.name().trim(),
            normalizeEmail(request.email()),
            trimToNull(request.contactNumber()),
            warehouse
        );
        user.updateProfile(user.getName(), user.getEmail(), user.getContactNumber(), user.getRole(), normalizeStatus(request.status()), warehouse);
        return UserResponse.from(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        validateUniqueEmail(request.email(), id);
        Warehouse warehouse = resolveWarehouse(request.warehouseId());
        Role role = request.role() == null ? user.getRole() : request.role();
        user.updateProfile(
            request.name().trim(),
            normalizeEmail(request.email()),
            trimToNull(request.contactNumber()),
            role,
            normalizeStatus(request.status()),
            warehouse
        );
        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(user);
    }

    private Warehouse resolveWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            return null;
        }
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
    }

    private void validateUniqueEmail(String email, Long currentUserId) {
        String normalized = normalizeEmail(email);
        if (normalized == null) {
            return;
        }
        appUserRepository.findByEmailIgnoreCase(normalized)
            .filter(user -> currentUserId == null || !user.getId().equals(currentUserId))
            .ifPresent(user -> {
                throw new DuplicateResourceException("Email already exists: " + normalized);
            });
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
