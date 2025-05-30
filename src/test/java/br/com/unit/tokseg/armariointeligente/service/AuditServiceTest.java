package br.com.unit.tokseg.armariointeligente.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuditServiceTest {

    @Autowired
    private AuditService auditService;

    @Test
    @WithMockUser(username = "test@example.com")
    public void testLogUserAction() {
        assertDoesNotThrow(() -> {
            auditService.logUserAction("CREATE", "Usuario", 1L, "Usuário criado");
        });
    }

    @Test
    public void testLogSystemEvent() {
        assertDoesNotThrow(() -> {
            auditService.logSystemEvent("STARTUP", "Sistema iniciado");
        });
    }

    @Test
    public void testLogSecurityEvent() {
        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_FAILED", "test@example.com", "Senha incorreta");
        });
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testLogDataAccess() {
        assertDoesNotThrow(() -> {
            auditService.logDataAccess("READ", "Usuario", 1L);
        });
    }
}
