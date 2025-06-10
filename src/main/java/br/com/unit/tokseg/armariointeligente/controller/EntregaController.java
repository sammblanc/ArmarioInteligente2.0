// Arquivo: src/main/java/br/com/unit/tokseg/armariointeligente/controller/EntregaController.java
package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Entrega;
import br.com.unit.tokseg.armariointeligente.model.StatusEntrega;
import br.com.unit.tokseg.armariointeligente.model.Usuario; // Importar Usuario
import br.com.unit.tokseg.armariointeligente.model.Compartimento; // Importar Compartimento
import br.com.unit.tokseg.armariointeligente.dto.EntregaDTO;
import br.com.unit.tokseg.armariointeligente.service.EntregaService;
import br.com.unit.tokseg.armariointeligente.service.AuditService;
// Removido UsuarioService já que a verificação isCurrentUser foi movida para EntregaService
// import br.com.unit.tokseg.armariointeligente.service.UsuarioService;
// import br.com.unit.tokseg.armariointeligente.service.EntregaServiceImpl; // Não é necessário se o bean for resolvido por EntregaService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List; // Importar List
import java.util.Optional;
import java.util.stream.Collectors; // Importar Collectors

@RestController
@RequestMapping("/api/v1/entregas")
@Tag(name = "Entregas", description = "Endpoints para gerenciamento de entregas")
public class EntregaController {

    @Autowired
    private EntregaService entregaService; // Interface é suficiente para injeção

    @Autowired
    private AuditService auditService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Registrar entrega", description = "Registra uma nova entrega no sistema")
    public ResponseEntity<EntregaDTO> registrarEntrega(@Valid @RequestBody EntregaDTO entregaDTO) {
        Entrega novaEntrega = entregaService.registrarEntrega(entregaDTO);
        auditService.logUserAction("CREATE", "Entrega", novaEntrega.getId(),
                "Entrega registrada: " + novaEntrega.getCodigoRastreio());
        return ResponseEntity.ok(convertToDTO(novaEntrega));
    }

    @PutMapping("/{id}/retirada")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('CLIENTE') or hasRole('ENTREGADOR')")
    @Operation(summary = "Registrar retirada", description = "Registra a retirada de uma entrega")
    public ResponseEntity<EntregaDTO> registrarRetirada(
            @Parameter(description = "ID da entrega") @PathVariable Long id,
            @Parameter(description = "Código de acesso do compartimento") @RequestParam String codigoAcesso) {
        Entrega entregaAtualizada = entregaService.registrarRetirada(id, codigoAcesso);
        auditService.logUserAction("UPDATE", "Entrega", id,
                "Entrega retirada: " + entregaAtualizada.getCodigoRastreio());
        return ResponseEntity.ok(convertToDTO(entregaAtualizada));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Cancelar entrega", description = "Cancela uma entrega registrada")
    public ResponseEntity<EntregaDTO> cancelarEntrega(
            @Parameter(description = "ID da entrega") @PathVariable Long id) {
        Entrega entregaAtualizada = entregaService.cancelarEntrega(id);
        auditService.logUserAction("CANCEL", "Entrega", id,
                "Entrega cancelada: " + entregaAtualizada.getCodigoRastreio());
        return ResponseEntity.ok(convertToDTO(entregaAtualizada));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Listar entregas", description = "Lista todas as entregas registradas no sistema")
    public ResponseEntity<List<EntregaDTO>> listarEntregas() {
        List<EntregaDTO> dtos = entregaService.listarEntregas().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/compartimento/{compartimentoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Listar entregas por compartimento", description = "Lista entregas de um compartimento específico")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorCompartimento(
            @Parameter(description = "ID do compartimento") @PathVariable Long compartimentoId) {
        List<EntregaDTO> dtos = entregaService.listarEntregasPorCompartimento(compartimentoId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/entregador/{entregadorId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Listar entregas por entregador", description = "Lista entregas realizadas por um entregador específico")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorEntregador(
            @Parameter(description = "ID do entregador") @PathVariable Long entregadorId) {
        List<EntregaDTO> dtos = entregaService.listarEntregasPorEntregador(entregadorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/destinatario/{destinatarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR') or @entregaService.isDestinatarioDaEntregaOuAdmin(#destinatarioId, authentication.principal)")
    @Operation(summary = "Listar entregas por destinatário", description = "Lista entregas destinadas a um usuário específico")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorDestinatario(
            @Parameter(description = "ID do destinatário") @PathVariable Long destinatarioId) {
        List<EntregaDTO> dtos = entregaService.listarEntregasPorDestinatario(destinatarioId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Listar entregas por status", description = "Lista entregas filtradas por status")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorStatus(
            @Parameter(description = "Status da entrega") @PathVariable StatusEntrega status) {
        List<EntregaDTO> dtos = entregaService.listarEntregasPorStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR') or @entregaService.isDestinatario(#id, authentication.principal)")
    @Operation(summary = "Buscar entrega por ID", description = "Busca uma entrega pelo seu ID")
    public ResponseEntity<EntregaDTO> buscarEntregaPorId(
            @Parameter(description = "ID da entrega") @PathVariable Long id) {
        Optional<Entrega> entregaOpt = entregaService.buscarEntregaPorId(id);
        if (entregaOpt.isPresent()) {
            Entrega entrega = entregaOpt.get();
            auditService.logDataAccess("READ", "Entrega", entrega.getId());
            return ResponseEntity.ok(convertToDTO(entrega));
        } else {
            throw new ResourceNotFoundException("Entrega", "id", id);
        }
    }

    @GetMapping("/rastreio/{codigoRastreio}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR') or hasRole('CLIENTE')")
    @Operation(summary = "Buscar entrega por código de rastreio", description = "Busca uma entrega pelo seu código de rastreio")
    public ResponseEntity<EntregaDTO> buscarEntregaPorCodigoRastreio(
            @Parameter(description = "Código de rastreio") @PathVariable String codigoRastreio) {
        Optional<Entrega> entregaOpt = entregaService.buscarEntregaPorCodigoRastreio(codigoRastreio);
        if (entregaOpt.isPresent()) {
            Entrega entrega = entregaOpt.get();
            auditService.logDataAccess("READ", "Entrega", entrega.getId());
            return ResponseEntity.ok(convertToDTO(entrega));
        } else {
            throw new ResourceNotFoundException("Entrega", "código de rastreio", codigoRastreio);
        }
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Listar entregas por período", description = "Lista entregas realizadas em um período específico")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorPeriodo(
            @Parameter(description = "Data de início (formato ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data de fim (formato ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<EntregaDTO> dtos = entregaService.listarEntregasPorPeriodo(inicio, fim).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private EntregaDTO convertToDTO(Entrega entrega) {
        EntregaDTO dto = new EntregaDTO();
        dto.setId(entrega.getId());
        dto.setCodigoRastreio(entrega.getCodigoRastreio());
        // if (entrega.getDescricao() != null) { // Se o campo descricao existir em Entrega.java
        //     dto.setDescricao(entrega.getDescricao());
        // }
        dto.setDataEntrega(entrega.getDataEntrega());
        dto.setDataRetirada(entrega.getDataRetirada());
        dto.setObservacao(entrega.getObservacao());
        dto.setStatus(entrega.getStatus());

        if (entrega.getCompartimento() != null) {
            dto.setCompartimentoId(entrega.getCompartimento().getId());
            dto.setCompartimentoNumero(entrega.getCompartimento().getNumero());
        }
        if (entrega.getEntregador() != null) {
            dto.setEntregadorId(entrega.getEntregador().getId());
            dto.setEntregadorNome(entrega.getEntregador().getNome());
        }
        if (entrega.getDestinatario() != null) {
            dto.setDestinatarioId(entrega.getDestinatario().getId());
            dto.setDestinatarioNome(entrega.getDestinatario().getNome());
        }
        return dto;
    }
}
