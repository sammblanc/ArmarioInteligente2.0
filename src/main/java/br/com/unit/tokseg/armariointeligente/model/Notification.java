package br.com.unit.tokseg.armariointeligente.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tipo de notificação é obrigatório")
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @NotBlank(message = "Destinatário é obrigatório")
    @Column(name = "destinatario", nullable = false)
    private String destinatario;

    @NotBlank(message = "Assunto é obrigatório")
    @Column(name = "assunto", nullable = false)
    private String assunto;

    @Column(name = "conteudo", columnDefinition = "TEXT")
    private String conteudo;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusNotification status;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "erro_mensagem")
    private String erroMensagem;

    // Referências opcionais para rastreabilidade
    @Column(name = "entrega_id")
    private Long entregaId;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusNotification.PENDENTE;
        }
    }
}
