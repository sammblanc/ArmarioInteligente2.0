package br.com.unit.tokseg.armariointeligente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "condominios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column
    private String cep;

    @Column
    private String cidade;

    @Column
    private String estado;

    @Column
    private String telefone;

    @Column
    private String email;

    @OneToMany(mappedBy = "condominio", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Armario> armarios;
}
