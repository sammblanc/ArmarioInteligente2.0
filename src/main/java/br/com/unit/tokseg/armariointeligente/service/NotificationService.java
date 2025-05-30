package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.model.Entrega;
import br.com.unit.tokseg.armariointeligente.model.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.time.format.DateTimeFormatter;

@Service
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async("emailExecutor")
    public void enviarNotificacaoEntregaRealizada(Entrega entrega) {
        try {
            if (entrega.getDestinatario() == null || entrega.getDestinatario().getEmail() == null) {
                logger.warn("Não foi possível enviar notificação de entrega: destinatário ou e-mail nulo para entrega ID {}", entrega.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(entrega.getDestinatario().getEmail());
            message.setSubject("Armário Inteligente: Sua Encomenda Chegou!");
            
            StringBuilder body = new StringBuilder();
            body.append("Olá ").append(entrega.getDestinatario().getNome()).append(",\n\n");
            body.append("Sua encomenda chegou e está disponível para retirada!\n\n");
            body.append("Detalhes da entrega:\n");
            body.append("• Código de rastreio: ").append(entrega.getCodigoRastreio()).append("\n");
            body.append("• Compartimento: ").append(entrega.getCompartimento().getNumero()).append("\n");
            body.append("• Armário: ").append(entrega.getCompartimento().getArmario().getIdentificacao()).append("\n");
            body.append("• Localização: ").append(entrega.getCompartimento().getArmario().getLocalizacao()).append("\n");
            body.append("• Código de acesso: ").append(entrega.getCompartimento().getCodigoAcesso()).append("\n");
            
            if (entrega.getObservacao() != null && !entrega.getObservacao().isEmpty()) {
                body.append("• Observações: ").append(entrega.getObservacao()).append("\n");
            }
            
            body.append("\nPara retirar sua encomenda, dirija-se ao armário e utilize o código de acesso fornecido.\n\n");
            body.append("Atenciosamente,\n");
            body.append("Equipe Armário Inteligente");

            message.setText(body.toString());
            
            mailSender.send(message);
            logger.info("Notificação de entrega realizada enviada com sucesso para {} (Entrega ID: {})", 
                       entrega.getDestinatario().getEmail(), entrega.getId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de entrega realizada para entrega ID {}: {}", 
                        entrega.getId(), e.getMessage(), e);
        }
    }

    @Async("emailExecutor")
    public void enviarNotificacaoEncomendaRetirada(Entrega entrega) {
        try {
            if (entrega.getDestinatario() == null || entrega.getDestinatario().getEmail() == null) {
                logger.warn("Não foi possível enviar notificação de retirada: destinatário ou e-mail nulo para entrega ID {}", entrega.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(entrega.getDestinatario().getEmail());
            message.setSubject("Armário Inteligente: Encomenda Retirada!");
            
            StringBuilder body = new StringBuilder();
            body.append("Olá ").append(entrega.getDestinatario().getNome()).append(",\n\n");
            body.append("Confirmamos que sua encomenda foi retirada com sucesso!\n\n");
            body.append("Detalhes da retirada:\n");
            body.append("• Código de rastreio: ").append(entrega.getCodigoRastreio()).append("\n");
            body.append("• Compartimento: ").append(entrega.getCompartimento().getNumero()).append("\n");
            
            if (entrega.getDataRetirada() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                body.append("• Data/Hora da retirada: ").append(entrega.getDataRetirada().format(formatter)).append("\n");
            }
            
            body.append("\nObrigado por utilizar nossos serviços!\n\n");
            body.append("Atenciosamente,\n");
            body.append("Equipe Armário Inteligente");

            message.setText(body.toString());
            
            mailSender.send(message);
            logger.info("Notificação de encomenda retirada enviada com sucesso para {} (Entrega ID: {})", 
                       entrega.getDestinatario().getEmail(), entrega.getId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de encomenda retirada para entrega ID {}: {}", 
                        entrega.getId(), e.getMessage(), e);
        }
    }

    @Async("emailExecutor")
    public void enviarNotificacaoReservaConfirmada(Reserva reserva) {
        try {
            if (reserva.getUsuario() == null || reserva.getUsuario().getEmail() == null) {
                logger.warn("Não foi possível enviar notificação de reserva confirmada: usuário ou e-mail nulo para reserva ID {}", reserva.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reserva.getUsuario().getEmail());
            message.setSubject("Armário Inteligente: Reserva Confirmada!");
            
            StringBuilder body = new StringBuilder();
            body.append("Olá ").append(reserva.getUsuario().getNome()).append(",\n\n");
            body.append("Sua reserva foi confirmada com sucesso!\n\n");
            body.append("Detalhes da reserva:\n");
            body.append("• Compartimento: ").append(reserva.getCompartimento().getNumero()).append("\n");
            body.append("• Armário: ").append(reserva.getCompartimento().getArmario().getIdentificacao()).append("\n");
            body.append("• Localização: ").append(reserva.getCompartimento().getArmario().getLocalizacao()).append("\n");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            body.append("• Período da reserva:\n");
            body.append("  - Início: ").append(reserva.getDataInicio().format(formatter)).append("\n");
            body.append("  - Fim: ").append(reserva.getDataFim().format(formatter)).append("\n");
            
            body.append("• Código de acesso: ").append(reserva.getCompartimento().getCodigoAcesso()).append("\n");
            
            if (reserva.getObservacao() != null && !reserva.getObservacao().isEmpty()) {
                body.append("• Observações: ").append(reserva.getObservacao()).append("\n");
            }
            
            body.append("\nUtilize o código de acesso fornecido para acessar o compartimento durante o período reservado.\n\n");
            body.append("Atenciosamente,\n");
            body.append("Equipe Armário Inteligente");

            message.setText(body.toString());
            
            mailSender.send(message);
            logger.info("Notificação de reserva confirmada enviada com sucesso para {} (Reserva ID: {})", 
                       reserva.getUsuario().getEmail(), reserva.getId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de reserva confirmada para reserva ID {}: {}", 
                        reserva.getId(), e.getMessage(), e);
        }
    }

    @Async("emailExecutor")
    public void enviarNotificacaoReservaCancelada(Reserva reserva) {
        try {
            if (reserva.getUsuario() == null || reserva.getUsuario().getEmail() == null) {
                logger.warn("Não foi possível enviar notificação de reserva cancelada: usuário ou e-mail nulo para reserva ID {}", reserva.getId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reserva.getUsuario().getEmail());
            message.setSubject("Armário Inteligente: Reserva Cancelada");
            
            StringBuilder body = new StringBuilder();
            body.append("Olá ").append(reserva.getUsuario().getNome()).append(",\n\n");
            body.append("Informamos que sua reserva foi cancelada.\n\n");
            body.append("Detalhes da reserva cancelada:\n");
            body.append("• Compartimento: ").append(reserva.getCompartimento().getNumero()).append("\n");
            body.append("• Armário: ").append(reserva.getCompartimento().getArmario().getIdentificacao()).append("\n");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            body.append("• Período original da reserva:\n");
            body.append("  - Início: ").append(reserva.getDataInicio().format(formatter)).append("\n");
            body.append("  - Fim: ").append(reserva.getDataFim().format(formatter)).append("\n");
            
            body.append("\nSe você não solicitou este cancelamento, entre em contato conosco.\n\n");
            body.append("Atenciosamente,\n");
            body.append("Equipe Armário Inteligente");

            message.setText(body.toString());
            
            mailSender.send(message);
            logger.info("Notificação de reserva cancelada enviada com sucesso para {} (Reserva ID: {})", 
                       reserva.getUsuario().getEmail(), reserva.getId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de reserva cancelada para reserva ID {}: {}", 
                        reserva.getId(), e.getMessage(), e);
        }
    }
}
