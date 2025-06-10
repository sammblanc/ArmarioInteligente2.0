package br.com.unit.tokseg.armariointeligente.repository;

import br.com.unit.tokseg.armariointeligente.model.Notification;
import br.com.unit.tokseg.armariointeligente.model.StatusNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByDestinatarioContainingIgnoreCase(String destinatario, Pageable pageable);

    Page<Notification> findByStatus(StatusNotification status, Pageable pageable);

    Page<Notification> findByTipo(String tipo, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.dataCriacao BETWEEN :dataInicio AND :dataFim")
    Page<Notification> findByDataCriacaoBetween(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    List<Notification> findByStatusAndTentativasLessThan(StatusNotification status, Integer maxTentativas);

    @Query("SELECT n FROM Notification n WHERE n.usuarioId = :usuarioId ORDER BY n.dataCriacao DESC")
    Page<Notification> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.entregaId = :entregaId ORDER BY n.dataCriacao DESC")
    List<Notification> findByEntregaId(@Param("entregaId") Long entregaId);

    @Query("SELECT n FROM Notification n WHERE n.reservaId = :reservaId ORDER BY n.dataCriacao DESC")
    List<Notification> findByReservaId(@Param("reservaId") Long reservaId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    Long countByStatus(@Param("status") StatusNotification status);
}
