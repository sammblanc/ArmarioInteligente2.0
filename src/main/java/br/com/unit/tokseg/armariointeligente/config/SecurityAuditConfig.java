package br.com.unit.tokseg.armariointeligente.config;

import br.com.unit.tokseg.armariointeligente.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class SecurityAuditConfig {

    @Autowired
    private AuditService auditService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        auditService.logSecurityEvent("LOGIN_SUCCESS", username, "Login realizado com sucesso");
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        auditService.logSecurityEvent("LOGIN_FAILED", username, "Tentativa de login com credenciais inválidas");
    }
}
