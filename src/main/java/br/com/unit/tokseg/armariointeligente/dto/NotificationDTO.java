package br.com.unit.tokseg.armariointeligente.dto;

import br.com.unit.tokseg.armariointeligente.model.StatusNotification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    @NotBlank(message = "Tipo de notificação é obrigatório")
    private String tipo;

    @NotBlank(message = "Destinatário é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String destinatario;

    @NotBlank(message = "Assunto é obrigatório")
    private String assunto;

    private String conteudo;

    @NotNull(message = "Status é obrigatório")
    private StatusNotification status;

    private LocalDateTime dataEnvio;
    private LocalDateTime dataCriacao;
    private Integer tentativas;
    private String erroMensagem;

    // Referências opcionais
    private Long entregaId;
    private Long reservaId;
    private Long usuarioId;
}
