package com.infotact.wms.service;

import com.infotact.wms.api.dto.AuthResponse;
import com.infotact.wms.api.dto.RegisterRequest;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.domain.Role;
import com.infotact.wms.domain.Warehouse;
import com.infotact.wms.exception.DuplicateResourceException;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.WarehouseRepository;
import com.infotact.wms.security.JwtService;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final AppUserRepository appUserRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegistrationService(
        AppUserRepository appUserRepository,
        WarehouseRepository warehouseRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.warehouseRepository = warehouseRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();
        String warehouseCode = request.warehouseCode().trim().toUpperCase();

        // Validate uniqueness
        if (appUserRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already taken: " + username);
        }
        appUserRepository.findByEmailIgnoreCase(email)
            .ifPresent(existing -> {
                throw new DuplicateResourceException("Email already registered: " + email);
            });
        if (warehouseRepository.existsByCode(warehouseCode)) {
            throw new DuplicateResourceException("Warehouse code already exists: " + warehouseCode);
        }

        // Create warehouse
        Warehouse warehouse = warehouseRepository.save(
            new Warehouse(warehouseCode, request.warehouseName().trim(), trimToNull(request.warehouseAddress()))
        );

        // Create admin user
        AppUser user = new AppUser(
            username,
            passwordEncoder.encode(request.password()),
            Role.ADMIN,
            request.name().trim(),
            email,
            trimToNull(request.contactNumber()),
            warehouse
        );
        appUserRepository.save(user);

        // Generate JWT so user is logged in immediately
        List<String> roles = List.of("ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        return new AuthResponse(jwtService.generate(auth), username, roles);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
