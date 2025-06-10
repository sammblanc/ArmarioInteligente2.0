package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Reserva;
import br.com.unit.tokseg.armariointeligente.model.StatusReserva;
import br.com.unit.tokseg.armariointeligente.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Endpoints para gerenciamento de reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('CLIENTE')")
    @Operation(summary = "Criar reserva", description = "Cria uma nova reserva de compartimento")
    public ResponseEntity<?> criarReserva(@RequestBody Reserva reserva) {
        Reserva novaReserva = reservaService.criarReserva(reserva);
        return ResponseEntity.ok(novaReserva);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioServiceImpl.isCurrentUser(authentication.principal.id)")
    @Operation(summary = "Cancelar reserva", description = "Cancela uma reserva existente")
    public ResponseEntity<?> cancelarReserva(
            @Parameter(description = "ID da reserva") @PathVariable Long id) {
        Reserva reservaAtualizada = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(reservaAtualizada);
    }

    @PutMapping("/{id}/concluir")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Concluir reserva", description = "Marca uma reserva como concluída")
    public ResponseEntity<?> concluirReserva(
            @Parameter(description = "ID da reserva") @PathVariable Long id) {
        Reserva reservaAtualizada = reservaService.concluirReserva(id);
        return ResponseEntity.ok(reservaAtualizada);
    }

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Lista todas as reservas registradas no sistema")
    public ResponseEntity<?> listarReservas() {
        return ResponseEntity.ok(reservaService.listarReservas());
    }

    @GetMapping("/compartimento/{compartimentoId}")
    @Operation(summary = "Listar reservas por compartimento", description = "Lista reservas de um compartimento específico")
    public ResponseEntity<?> listarReservasPorCompartimento(
            @Parameter(description = "ID do compartimento") @PathVariable Long compartimentoId) {
        return ResponseEntity.ok(reservaService.listarReservasPorCompartimento(compartimentoId));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioServiceImpl.isCurrentUser(#usuarioId)")
    @Operation(summary = "Listar reservas por usuário", description = "Lista reservas feitas por um usuário específico")
    public ResponseEntity<?> listarReservasPorUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(reservaService.listarReservasPorUsuario(usuarioId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar reservas por status", description = "Lista reservas filtradas por status")
    public ResponseEntity<?> listarReservasPorStatus(
            @Parameter(description = "Status da reserva") @PathVariable StatusReserva status) {
        return ResponseEntity.ok(reservaService.listarReservasPorStatus(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioServiceImpl.isCurrentUser(authentication.principal.id)")
    @Operation(summary = "Buscar reserva por ID", description = "Busca uma reserva pelo seu ID")
    public ResponseEntity<?> buscarReservaPorId(
            @Parameter(description = "ID da reserva") @PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.buscarReservaPorId(id);
        if (reserva.isPresent()) {
            return ResponseEntity.ok(reserva.get());
        } else {
            throw new ResourceNotFoundException("Reserva", "id", id);
        }
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar reservas por período", description = "Lista reservas realizadas em um período específico")
    public ResponseEntity<?> listarReservasPorPeriodo(
            @Parameter(description = "Data de início (formato ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data de fim (formato ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(reservaService.listarReservasPorPeriodo(inicio, fim));
    }
}
