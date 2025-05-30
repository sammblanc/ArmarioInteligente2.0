package br.com.unit.tokseg.armariointeligente.repository;

import br.com.unit.tokseg.armariointeligente.model.Reserva;
import br.com.unit.tokseg.armariointeligente.model.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByCompartimentoId(Long compartimentoId);
    List<Reserva> findByUsuarioId(Long usuarioId);
    List<Reserva> findByStatus(StatusReserva status);
    List<Reserva> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);
}
