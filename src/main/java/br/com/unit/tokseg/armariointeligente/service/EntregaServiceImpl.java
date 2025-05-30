package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.model.Entrega;
import br.com.unit.tokseg.armariointeligente.repository.EntregaRepository;
import br.com.unit.tokseg.armariointeligente.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EntregaServiceImpl {

    @Autowired
    private EntregaRepository entregaRepository;

    public boolean isDestinatario(Long entregaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            Long usuarioId = ((UserDetailsImpl) principal).getId();
            Optional<Entrega> entrega = entregaRepository.findById(entregaId);
            
            return entrega.isPresent() && entrega.get().getDestinatario().getId().equals(usuarioId);
        }
        
        return false;
    }
}
