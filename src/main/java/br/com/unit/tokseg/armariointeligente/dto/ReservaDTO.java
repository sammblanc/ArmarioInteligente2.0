package br.com.unit.tokseg.armariointeligente.dto;

import br.com.unit.tokseg.armariointeligente.model.StatusReserva;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    private Long id;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser no futuro")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Future(message = "Data de fim deve ser no futuro")
    private LocalDateTime dataFim;

    @Size(max = 1000, message = "Observação deve ter no máximo 1000 caracteres")
    private String observacao;

    private StatusReserva status;

    @NotNull(message = "Compartimento é obrigatório")
    private Long compartimentoId;

    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;

    // Campos para exibição
    private String compartimentoNumero;
    private String usuarioNome;
}
