package br.com.unit.tokseg.armariointeligente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "armarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Armario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identificacao;

    @Column
    private String localizacao;

    @Column
    private String descricao;

    @Column
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    @OneToMany(mappedBy = "armario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Compartimento> compartimentos;
}
