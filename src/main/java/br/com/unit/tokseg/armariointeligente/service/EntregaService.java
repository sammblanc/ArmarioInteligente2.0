package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.dto.EntregaDTO;
import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import br.com.unit.tokseg.armariointeligente.repository.EntregaRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import br.com.unit.tokseg.armariointeligente.security.UserDetailsImpl;
import br.com.unit.tokseg.armariointeligente.util.ValidationUtils; // Mantendo a referência, assumindo que esta classe existe no seu projeto
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntregaService {

    private static final Logger logger = LoggerFactory.getLogger(EntregaService.class);

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private CompartimentoRepository compartimentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompartimentoService compartimentoService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Entrega registrarEntrega(EntregaDTO entregaDTO) { // Assinatura corrigida para aceitar EntregaDTO
        if (entregaDTO == null) {
            logger.error("Tentativa de registrar entrega com DTO nulo.");
            throw new BadRequestException("Dados da entrega não podem ser nulos");
        }

        // Validações usando ValidationUtils (se esta for sua intenção)
        ValidationUtils.validateNotBlank(entregaDTO.getCodigoRastreio(), "Código de rastreio do DTO");
        ValidationUtils.validateNotNull(entregaDTO.getCompartimentoId(), "ID do Compartimento do DTO");
        ValidationUtils.validateNotNull(entregaDTO.getEntregadorId(), "ID do Entregador do DTO");
        ValidationUtils.validateNotNull(entregaDTO.getDestinatarioId(), "ID do Destinatário do DTO");

        if (entregaDTO.getCodigoRastreio() == null || entregaDTO.getCodigoRastreio().isEmpty()) {
            throw new BadRequestException("Código de rastreio não pode ser nulo ou vazio");
        }
        // Outras validações de nulidade já estão cobertas por ValidationUtils.validateNotNull

        entregaRepository.findByCodigoRastreio(entregaDTO.getCodigoRastreio()).ifPresent(e -> {
            logger.warn("Tentativa de registrar entrega com código de rastreio duplicado: {}", entregaDTO.getCodigoRastreio());
            throw new ResourceAlreadyExistsException("Entrega", "código de rastreio", entregaDTO.getCodigoRastreio());
        });

        Compartimento compartimento = compartimentoRepository.findById(entregaDTO.getCompartimentoId())
                .orElseThrow(() -> {
                    logger.error("Compartimento não encontrado com ID: {}", entregaDTO.getCompartimentoId());
                    return new ResourceNotFoundException("Compartimento", "id", entregaDTO.getCompartimentoId());
                });

        if (compartimento.getOcupado()) {
            logger.warn("Tentativa de registrar entrega em compartimento já ocupado: ID {}", compartimento.getId());
            throw new BadRequestException("O compartimento selecionado já está ocupado: " + compartimento.getNumero());
        }

        Usuario entregador = usuarioRepository.findById(entregaDTO.getEntregadorId())
                .orElseThrow(() -> {
                    logger.error("Entregador não encontrado com ID: {}", entregaDTO.getEntregadorId());
                    return new ResourceNotFoundException("Entregador", "id", entregaDTO.getEntregadorId());
                });

        Usuario destinatario = usuarioRepository.findById(entregaDTO.getDestinatarioId())
                .orElseThrow(() -> {
                    logger.error("Destinatário não encontrado com ID: {}", entregaDTO.getDestinatarioId());
                    return new ResourceNotFoundException("Destinatário", "id", entregaDTO.getDestinatarioId());
                });

        if (!"Entregador".equals(entregador.getTipoUsuario().getNome()) &&
                !"Administrador".equals(entregador.getTipoUsuario().getNome())) {
            logger.warn("Usuário {} (ID: {}) tentou registrar entrega sem permissão.", entregador.getNome(), entregador.getId());
            throw new BadRequestException("O usuário não tem permissão para registrar entregas");
        }

        Entrega entrega = new Entrega();
        entrega.setCodigoRastreio(entregaDTO.getCodigoRastreio());
        // if (entregaDTO.getDescricao() != null) { // Se o campo descricao existir em EntregaDTO e Entrega.java
        // ValidationUtils.validateStringLength(entregaDTO.getDescricao(), "Descrição do DTO", 0, 1000);
        //     entrega.setDescricao(entregaDTO.getDescricao());
        // }
        if (entregaDTO.getObservacao() != null) {
            ValidationUtils.validateStringLength(entregaDTO.getObservacao(), "Observação do DTO", 0, 1000);
            entrega.setObservacao(entregaDTO.getObservacao());
        }

        entrega.setDataEntrega(LocalDateTime.now());
        entrega.setStatus(StatusEntrega.AGUARDANDO_RETIRADA);
        entrega.setCompartimento(compartimento);
        entrega.setEntregador(entregador);
        entrega.setDestinatario(destinatario);

        compartimentoService.atualizarStatusCompartimento(compartimento.getId(), true);
        compartimentoService.gerarNovoCodigoAcesso(compartimento.getId());

        Entrega novaEntregaSalva = entregaRepository.save(entrega);
        logger.info("Entrega registrada com sucesso - ID: {}, Código: {}, Destinatário Email: {}",
                novaEntregaSalva.getId(), novaEntregaSalva.getCodigoRastreio(),
                novaEntregaSalva.getDestinatario().getEmail());

        notificationService.enviarNotificacaoEntregaRealizada(novaEntregaSalva);

        return novaEntregaSalva;
    }

    @Transactional
    public Entrega registrarRetirada(Long id, String codigoAcesso) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));

        if (entrega.getStatus() != StatusEntrega.AGUARDANDO_RETIRADA) {
            logger.warn("Tentativa de retirar entrega ID {} com status inválido: {}", id, entrega.getStatus());
            throw new BadRequestException("Esta entrega não está aguardando retirada. Status atual: " + entrega.getStatus());
        }

        Compartimento compartimento = entrega.getCompartimento();
        if (compartimento.getCodigoAcesso() == null || !compartimento.getCodigoAcesso().equals(codigoAcesso)) {
            logger.warn("Tentativa de retirada da entrega ID {} com código de acesso inválido.", id);
            throw new BadRequestException("Código de acesso inválido para o compartimento " + compartimento.getNumero());
        }

        entrega.setDataRetirada(LocalDateTime.now());
        entrega.setStatus(StatusEntrega.RETIRADO);

        compartimentoService.atualizarStatusCompartimento(compartimento.getId(), false);

        Entrega entregaAtualizada = entregaRepository.save(entrega);
        logger.info("Entrega ID {} retirada com sucesso.", id);
        notificationService.enviarNotificacaoEncomendaRetirada(entregaAtualizada);
        return entregaAtualizada;
    }

    @Transactional
    public Entrega cancelarEntrega(Long id) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));

        if (entrega.getStatus() == StatusEntrega.RETIRADO || entrega.getStatus() == StatusEntrega.CANCELADO) {
            logger.warn("Tentativa de cancelar entrega ID {} que já está {} não permitida.", id, entrega.getStatus());
            throw new BadRequestException("Esta entrega não pode ser cancelada pois já foi " + entrega.getStatus().toString().toLowerCase() + ".");
        }

        if (entrega.getStatus() == StatusEntrega.AGUARDANDO_RETIRADA) {
            compartimentoService.atualizarStatusCompartimento(entrega.getCompartimento().getId(), false);
            logger.info("Compartimento ID {} liberado devido ao cancelamento da entrega ID {}.", entrega.getCompartimento().getId(), id);
        }

        entrega.setStatus(StatusEntrega.CANCELADO);
        logger.info("Entrega ID {} cancelada com sucesso.", id);
        // Se houver notificação para cancelamento, adicionar aqui
        // notificationService.enviarNotificacaoEntregaCancelada(entrega);

        return entregaRepository.save(entrega);
    }

    @Transactional
    public List<Entrega> listarEntregas() {
        logger.debug("Listando todas as entregas.");
        return entregaRepository.findAll();
    }

    @Transactional
    public List<Entrega> listarEntregasPorCompartimento(Long compartimentoId) {
        if (!compartimentoRepository.existsById(compartimentoId)) {
            throw new ResourceNotFoundException("Compartimento", "id", compartimentoId);
        }
        logger.debug("Listando entregas para o compartimento ID: {}", compartimentoId);
        return entregaRepository.findByCompartimentoId(compartimentoId);
    }

    @Transactional
    public List<Entrega> listarEntregasPorEntregador(Long entregadorId) {
        if (!usuarioRepository.existsById(entregadorId)) {
            throw new ResourceNotFoundException("Entregador", "id", entregadorId);
        }
        logger.debug("Listando entregas para o entregador ID: {}", entregadorId);
        return entregaRepository.findByEntregadorId(entregadorId);
    }

    @Transactional
    public List<Entrega> listarEntregasPorDestinatario(Long destinatarioId) {
        if (!usuarioRepository.existsById(destinatarioId)) {
            throw new ResourceNotFoundException("Destinatário", "id", destinatarioId);
        }
        logger.debug("Listando entregas para o destinatário ID: {}", destinatarioId);
        return entregaRepository.findByDestinatarioId(destinatarioId);
    }

    @Transactional
    public List<Entrega> listarEntregasPorStatus(StatusEntrega status) {
        logger.debug("Listando entregas com status: {}", status);
        return entregaRepository.findByStatus(status);
    }

    @Transactional
    public Optional<Entrega> buscarEntregaPorId(Long id) {
        logger.debug("Buscando entrega por ID: {}", id);
        return entregaRepository.findById(id);
    }

    @Transactional
    public Optional<Entrega> buscarEntregaPorCodigoRastreio(String codigoRastreio) {
        logger.debug("Buscando entrega por código de rastreio: {}", codigoRastreio);
        return entregaRepository.findByCodigoRastreio(codigoRastreio);
    }

    @Transactional
    public List<Entrega> listarEntregasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new BadRequestException("Datas de início e fim são obrigatórias para busca por período.");
        }
        if (inicio.isAfter(fim)) {
            throw new BadRequestException("Data de início não pode ser posterior à data de fim.");
        }
        logger.debug("Listando entregas entre {} e {}", inicio, fim);
        return entregaRepository.findByDataEntregaBetween(inicio, fim);
    }


    public boolean isDestinatarioDaEntregaOuAdmin(Long destinatarioIdQuery, Object principal) {
        if (principal instanceof UserDetailsImpl userDetails) {
            if (userDetails.getId().equals(destinatarioIdQuery)) {
                return true; // O usuário logado é o destinatário da consulta
            }
            // Verifica se o usuário logado tem a role de ADMINISTRADOR
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> "ROLE_ADMINISTRADOR".equals(grantedAuthority.getAuthority()));
            if (isAdmin) {
                logger.debug("Acesso permitido para ADMIN (ID: {}) à lista de entregas do destinatário ID: {}", userDetails.getId(), destinatarioIdQuery);
            }
            return isAdmin;
        }
        return false;
    }

    public boolean isDestinatario(Long entregaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long usuarioLogadoId = userDetails.getId();

        Optional<Entrega> entregaOpt = entregaRepository.findById(entregaId);

        if (entregaOpt.isPresent()) {
            boolean isDest = entregaOpt.get().getDestinatario().getId().equals(usuarioLogadoId);
            if (isDest) {
                logger.debug("Usuário ID {} é o destinatário da entrega ID {}", usuarioLogadoId, entregaId);
            }
            return isDest;
        }
        return false;
    }
}
