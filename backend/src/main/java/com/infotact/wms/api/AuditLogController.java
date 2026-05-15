package com.infotact.wms.api;

import com.infotact.wms.api.dto.AuditLogResponse;
import com.infotact.wms.service.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> list(@RequestParam(defaultValue = "50") int limit) {
        return auditLogService.getRecentLogs(Math.min(limit, 200));
    }
}
