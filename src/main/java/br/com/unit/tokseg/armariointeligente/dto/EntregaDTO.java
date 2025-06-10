package br.com.unit.tokseg.armariointeligente.dto;

import br.com.unit.tokseg.armariointeligente.model.StatusEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaDTO {

    private Long id;

    @NotBlank(message = "Código de rastreio é obrigatório")
    @Size(max = 255, message = "Código de rastreio deve ter no máximo 255 caracteres")
    private String codigoRastreio;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private LocalDateTime dataEntrega;
    private LocalDateTime dataRetirada;

    @Size(max = 1000, message = "Observação deve ter no máximo 1000 caracteres")
    private String observacao;

    private StatusEntrega status;

    @NotNull(message = "Compartimento é obrigatório")
    private Long compartimentoId;

    @NotNull(message = "Entregador é obrigatório")
    private Long entregadorId;

    @NotNull(message = "Destinatário é obrigatório")
    private Long destinatarioId;

    // Campos para exibição
    private String compartimentoNumero;
    private String entregadorNome;
    private String destinatarioNome;
}
