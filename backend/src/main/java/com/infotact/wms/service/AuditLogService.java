package com.infotact.wms.service;

import com.infotact.wms.api.dto.AuditLogResponse;
import com.infotact.wms.repository.InventoryTransactionRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final InventoryTransactionRepository transactionRepository;

    public AuditLogService(InventoryTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs(int limit) {
        return transactionRepository
            .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
            .stream()
            .map(AuditLogResponse::from)
            .toList();
    }
}
