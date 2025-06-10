package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.dto.NotificationDTO;
import br.com.unit.tokseg.armariointeligente.dto.NotificationRequestDTO;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Notification;
import br.com.unit.tokseg.armariointeligente.model.StatusNotification;
import br.com.unit.tokseg.armariointeligente.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHistoryService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Page<NotificationDTO> listarTodas(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Page<NotificationDTO> buscarPorDestinatario(String destinatario, Pageable pageable) {
        return notificationRepository.findByDestinatarioContainingIgnoreCase(destinatario, pageable)
                .map(this::convertToDTO);
    }

    public Page<NotificationDTO> buscarPorStatus(StatusNotification status, Pageable pageable) {
        return notificationRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    public Page<NotificationDTO> buscarPorTipo(String tipo, Pageable pageable) {
        return notificationRepository.findByTipo(tipo, pageable)
                .map(this::convertToDTO);
    }

    public Page<NotificationDTO> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        return notificationRepository.findByDataCriacaoBetween(dataInicio, dataFim, pageable)
                .map(this::convertToDTO);
    }

    public NotificationDTO buscarPorId(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada com ID: " + id));
        return convertToDTO(notification);
    }

    @Async("emailExecutor")
    public void enviarNotificacaoPersonalizada(NotificationRequestDTO request) {
        Notification notification = new Notification();
        notification.setTipo(request.getTipo());
        notification.setDestinatario(request.getDestinatario());
        notification.setAssunto(request.getAssunto());
        notification.setConteudo(request.getConteudo());
        notification.setEntregaId(request.getEntregaId());
        notification.setReservaId(request.getReservaId());
        notification.setUsuarioId(request.getUsuarioId());
        notification.setStatus(StatusNotification.PENDENTE);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getDestinatario());
            message.setSubject(request.getAssunto());
            message.setText(request.getConteudo());

            mailSender.send(message);

            notification.setStatus(StatusNotification.ENVIADA);
            notification.setDataEnvio(LocalDateTime.now());
            notification.setTentativas(1);

            logger.info("Notificação personalizada enviada com sucesso para: {}", request.getDestinatario());

        } catch (Exception e) {
            notification.setStatus(StatusNotification.ERRO);
            notification.setErroMensagem(e.getMessage());
            notification.setTentativas(1);

            logger.error("Erro ao enviar notificação personalizada para {}: {}", 
                        request.getDestinatario(), e.getMessage(), e);
        }

        notificationRepository.save(notification);
    }

    public void reenviarNotificacao(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada com ID: " + id));

        if (notification.getStatus() == StatusNotification.ENVIADA) {
            throw new IllegalStateException("Notificação já foi enviada com sucesso");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getDestinatario());
            message.setSubject(notification.getAssunto());
            message.setText(notification.getConteudo());

            mailSender.send(message);

            notification.setStatus(StatusNotification.ENVIADA);
            notification.setDataEnvio(LocalDateTime.now());
            notification.setTentativas(notification.getTentativas() + 1);
            notification.setErroMensagem(null);

            logger.info("Notificação reenviada com sucesso para: {} (ID: {})", 
                       notification.getDestinatario(), id);

        } catch (Exception e) {
            notification.setStatus(StatusNotification.ERRO);
            notification.setErroMensagem(e.getMessage());
            notification.setTentativas(notification.getTentativas() + 1);

            logger.error("Erro ao reenviar notificação ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao reenviar notificação: " + e.getMessage());
        }

        notificationRepository.save(notification);
    }

    public void cancelarNotificacao(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada com ID: " + id));

        if (notification.getStatus() == StatusNotification.ENVIADA) {
            throw new IllegalStateException("Não é possível cancelar uma notificação já enviada");
        }

        notification.setStatus(StatusNotification.CANCELADA);
        notificationRepository.save(notification);

        logger.info("Notificação cancelada: ID {}", id);
    }

    public List<NotificationDTO> buscarNotificacoesPendentes() {
        List<Notification> pendentes = notificationRepository
                .findByStatusAndTentativasLessThan(StatusNotification.PENDENTE, 3);
        
        return pendentes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void salvarHistoricoNotificacao(String tipo, String destinatario, String assunto, 
                                         String conteudo, StatusNotification status, 
                                         Long entregaId, Long reservaId, Long usuarioId) {
        Notification notification = new Notification();
        notification.setTipo(tipo);
        notification.setDestinatario(destinatario);
        notification.setAssunto(assunto);
        notification.setConteudo(conteudo);
        notification.setStatus(status);
        notification.setEntregaId(entregaId);
        notification.setReservaId(reservaId);
        notification.setUsuarioId(usuarioId);
        notification.setTentativas(1);

        if (status == StatusNotification.ENVIADA) {
            notification.setDataEnvio(LocalDateTime.now());
        }

        notificationRepository.save(notification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTipo(notification.getTipo());
        dto.setDestinatario(notification.getDestinatario());
        dto.setAssunto(notification.getAssunto());
        dto.setConteudo(notification.getConteudo());
        dto.setStatus(notification.getStatus());
        dto.setDataEnvio(notification.getDataEnvio());
        dto.setDataCriacao(notification.getDataCriacao());
        dto.setTentativas(notification.getTentativas());
        dto.setErroMensagem(notification.getErroMensagem());
        dto.setEntregaId(notification.getEntregaId());
        dto.setReservaId(notification.getReservaId());
        dto.setUsuarioId(notification.getUsuarioId());
        return dto;
    }
}
