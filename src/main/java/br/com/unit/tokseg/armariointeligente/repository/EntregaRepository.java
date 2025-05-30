package br.com.unit.tokseg.armariointeligente.repository;

import br.com.unit.tokseg.armariointeligente.model.Entrega;
import br.com.unit.tokseg.armariointeligente.model.StatusEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByCompartimentoId(Long compartimentoId);
    List<Entrega> findByEntregadorId(Long entregadorId);
    List<Entrega> findByDestinatarioId(Long destinatarioId);
    List<Entrega> findByStatus(StatusEntrega status);
    Optional<Entrega> findByCodigoRastreio(String codigoRastreio);
    List<Entrega> findByDataEntregaBetween(LocalDateTime inicio, LocalDateTime fim);
}
