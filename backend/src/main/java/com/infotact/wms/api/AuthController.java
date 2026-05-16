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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final AppUserRepository appUserRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, RegistrationService registrationService, AppUserRepository appUserRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.registrationService = registrationService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        List<String> roles = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> authority.replaceFirst("^ROLE_", ""))
            .toList();

        AppUser user = appUserRepository.findByUsername(authentication.getName()).orElse(null);
        Long warehouseId = (user != null && user.getWarehouse() != null) ? user.getWarehouse().getId() : null;
        String warehouseCode = (user != null && user.getWarehouse() != null) ? user.getWarehouse().getCode() : null;
        String warehouseName = (user != null && user.getWarehouse() != null) ? user.getWarehouse().getName() : null;

        return new AuthResponse(jwtService.generate(authentication), authentication.getName(), roles, warehouseId, warehouseCode, warehouseName);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }
}
