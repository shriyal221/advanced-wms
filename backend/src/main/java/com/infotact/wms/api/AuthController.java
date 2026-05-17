package com.infotact.wms.api;

import com.infotact.wms.api.dto.AuthResponse;
import com.infotact.wms.api.dto.LoginRequest;
import com.infotact.wms.api.dto.RegisterRequest;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.security.JwtService;
import com.infotact.wms.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
        JwtService jwtService,
        RegistrationService registrationService,
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.registrationService = registrationService;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        Optional<AppUser> candidate = appUserRepository.findByUsernameWithWarehouse(username);
        boolean active = candidate
            .map(user -> user.getStatus() == null || "ACTIVE".equalsIgnoreCase(user.getStatus()))
            .orElse(false);
        boolean passwordMatches = candidate
            .map(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
            .orElse(false);
        AppUser user = candidate
            .filter(ignored -> active)
            .filter(ignored -> passwordMatches)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        List<String> roles = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> authority.replaceFirst("^ROLE_", ""))
            .toList();

        Long warehouseId = user.getWarehouse() == null ? null : user.getWarehouse().getId();
        String warehouseCode = user.getWarehouse() == null ? null : user.getWarehouse().getCode();
        String warehouseName = user.getWarehouse() == null ? null : user.getWarehouse().getName();

        return new AuthResponse(jwtService.generate(authentication), user.getUsername(), roles, warehouseId, warehouseCode, warehouseName);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }
}
