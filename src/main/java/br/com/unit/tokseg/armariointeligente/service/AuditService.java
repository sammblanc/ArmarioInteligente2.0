package br.com.unit.tokseg.armariointeligente.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void logUserAction(String action, String entityType, Long entityId, String details) {
        String username = getCurrentUsername();
        auditLogger.info("USER_ACTION - User: {}, Action: {}, Entity: {} (ID: {}), Details: {}", 
                         username, action, entityType, entityId, details);
    }

    public void logSystemEvent(String event, String details) {
        auditLogger.info("SYSTEM_EVENT - Event: {}, Details: {}", event, details);
    }

    public void logSecurityEvent(String event, String username, String details) {
        auditLogger.warn("SECURITY_EVENT - Event: {}, User: {}, Details: {}", event, username, details);
    }

    public void logDataAccess(String operation, String entityType, Long entityId) {
        String username = getCurrentUsername();
        auditLogger.debug("DATA_ACCESS - User: {}, Operation: {}, Entity: {} (ID: {})", 
                          username, operation, entityType, entityId);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
