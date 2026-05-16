package com.infotact.wms.service;

import com.infotact.wms.api.dto.AuditLogResponse;
import com.infotact.wms.domain.AppUser;
import com.infotact.wms.repository.AppUserRepository;
import com.infotact.wms.repository.InventoryTransactionRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final InventoryTransactionRepository transactionRepository;
    private final AppUserRepository appUserRepository;

    public AuditLogService(InventoryTransactionRepository transactionRepository, AppUserRepository appUserRepository) {
        this.transactionRepository = transactionRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs(int limit) {
        AppUser currentUser = getCurrentUser();
        return transactionRepository
            .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
            .stream()
            .filter(tx -> {
                if (currentUser != null && currentUser.getWarehouse() != null
                        && tx.getStorageBin() != null) {
                    return tx.getStorageBin().getAisle().getZone().getWarehouse().getId()
                        .equals(currentUser.getWarehouse().getId());
                }
                return true;
            })
            .map(AuditLogResponse::from)
            .toList();
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return appUserRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
