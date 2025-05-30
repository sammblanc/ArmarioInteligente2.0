package br.com.unit.tokseg.armariointeligente.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo;
    private Long id;
    private String nome;
    private String email;
    private String tipoUsuario;
}
