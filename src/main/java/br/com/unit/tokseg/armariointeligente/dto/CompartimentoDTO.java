package br.com.unit.tokseg.armariointeligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompartimentoDTO {

    @NotBlank(message = "O número do compartimento é obrigatório")
    private String numero;

    private String tamanho;

    @NotNull(message = "O ID do armário é obrigatório para criar um compartimento")
    private Long armarioId;
}