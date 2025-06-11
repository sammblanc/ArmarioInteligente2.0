package br.com.unit.tokseg.armariointeligente.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArmarioDTO {

    private String identificacao;
    private String localizacao;
    private String descricao;
    private Boolean ativo = true;
    private Long condominioId; // Apenas o ID do condomínio

}