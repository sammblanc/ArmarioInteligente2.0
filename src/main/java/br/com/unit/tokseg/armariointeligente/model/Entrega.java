package br.com.unit.tokseg.armariointeligente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigoRastreio;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataEntrega;

    @Column
    private LocalDateTime dataRetirada;

    @Column(length = 1000)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEntrega status;

    @ManyToOne
    @JoinColumn(name = "compartimento_id", nullable = false)
    private Compartimento compartimento;

    @ManyToOne
    @JoinColumn(name = "entregador_id", nullable = false)
    private Usuario entregador;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;
}
