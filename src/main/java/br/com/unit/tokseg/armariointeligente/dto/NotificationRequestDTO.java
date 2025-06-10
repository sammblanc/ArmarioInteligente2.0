package br.com.unit.tokseg.armariointeligente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    @NotBlank(message = "Tipo de notificação é obrigatório")
    private String tipo;

    @NotBlank(message = "Destinatário é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String destinatario;

    @NotBlank(message = "Assunto é obrigatório")
    private String assunto;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String conteudo;

    // Referências opcionais para contexto
    private Long entregaId;
    private Long reservaId;
    private Long usuarioId;
}
