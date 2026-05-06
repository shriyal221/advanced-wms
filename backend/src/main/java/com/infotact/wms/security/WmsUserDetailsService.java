package com.infotact.wms.security;

import com.infotact.wms.domain.AppUser;
import com.infotact.wms.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WmsUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public WmsUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .disabled(!"ACTIVE".equalsIgnoreCase(user.getStatus()))
            .build();
    }
}
