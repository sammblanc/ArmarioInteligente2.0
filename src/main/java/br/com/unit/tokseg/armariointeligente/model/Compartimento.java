package br.com.unit.tokseg.armariointeligente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "compartimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compartimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;

    @Column
    private String tamanho; // P, M, G, etc.

    @Column
    private Boolean ocupado = false;

    @Column
    private String codigoAcesso;

    @ManyToOne
    @JoinColumn(name = "armario_id", nullable = false)
    private Armario armario;

    @OneToMany(mappedBy = "compartimento")
    @JsonIgnore
    private List<Entrega> entregas;
}
