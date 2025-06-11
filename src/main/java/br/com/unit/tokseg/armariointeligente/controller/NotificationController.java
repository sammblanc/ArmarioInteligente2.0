package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.dto.NotificationDTO;
import br.com.unit.tokseg.armariointeligente.dto.NotificationRequestDTO;
import br.com.unit.tokseg.armariointeligente.model.StatusNotification;
import br.com.unit.tokseg.armariointeligente.service.NotificationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notificações", description = "Gerenciamento de notificações do sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    @Autowired
    private NotificationHistoryService notificationHistoryService;

    @GetMapping
    @Operation(summary = "Listar todas as notificações", 
               description = "Retorna uma lista paginada de todas as notificações do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de notificações retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PORTEIRO')")
    public ResponseEntity<Page<NotificationDTO>> listarTodas(
            @Parameter(description = "Número da página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "dataCriacao") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationDTO> notifications = notificationHistoryService.listarTodas(pageable);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar notificação por ID", 
               description = "Retorna os detalhes de uma notificação específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação encontrada"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PORTEIRO')")
    public ResponseEntity<NotificationDTO> buscarPorId(
            @Parameter(description = "ID da notificação")
            @PathVariable Long id) {
        
        NotificationDTO notification = notificationHistoryService.buscarPorId(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar notificações por filtros", 
               description = "Busca notificações por destinatário, status, tipo ou período")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PORTEIRO')")
    public ResponseEntity<Page<NotificationDTO>> buscarComFiltros(
            @Parameter(description = "Email do destinatário")
            @RequestParam(required = false) String destinatario,
            @Parameter(description = "Status da notificação")
            @RequestParam(required = false) StatusNotification status,
            @Parameter(description = "Tipo da notificação")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Data de início (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data de fim (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataCriacao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationDTO> notifications;

        if (destinatario != null && !destinatario.trim().isEmpty()) {
            notifications = notificationHistoryService.buscarPorDestinatario(destinatario, pageable);
        } else if (status != null) {
            notifications = notificationHistoryService.buscarPorStatus(status, pageable);
        } else if (tipo != null && !tipo.trim().isEmpty()) {
            notifications = notificationHistoryService.buscarPorTipo(tipo, pageable);
        } else if (dataInicio != null && dataFim != null) {
            notifications = notificationHistoryService.buscarPorPeriodo(dataInicio, dataFim, pageable);
        } else {
            notifications = notificationHistoryService.listarTodas(pageable);
        }

        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/send")
    @Operation(summary = "Enviar notificação personalizada", 
               description = "Envia uma notificação personalizada para um destinatário específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Notificação aceita para envio"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PORTEIRO')")
    public ResponseEntity<Map<String, String>> enviarNotificacaoPersonalizada(
            @Parameter(description = "Dados da notificação a ser enviada")
            @Valid @RequestBody NotificationRequestDTO request) {
        
        notificationHistoryService.enviarNotificacaoPersonalizada(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação aceita para envio");
        response.put("destinatario", request.getDestinatario());
        response.put("tipo", request.getTipo());
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/{id}/resend")
    @Operation(summary = "Reenviar notificação", 
               description = "Reenvia uma notificação que falhou anteriormente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação reenviada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Notificação não pode ser reenviada"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, String>> reenviarNotificacao(
            @Parameter(description = "ID da notificação a ser reenviada")
            @PathVariable Long id) {
        
        notificationHistoryService.reenviarNotificacao(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação reenviada com sucesso");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancelar notificação", 
               description = "Cancela uma notificação pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação cancelada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Notificação não pode ser cancelada"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, String>> cancelarNotificacao(
            @Parameter(description = "ID da notificação a ser cancelada")
            @PathVariable Long id) {
        
        notificationHistoryService.cancelarNotificacao(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação cancelada com sucesso");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar notificações pendentes", 
               description = "Retorna todas as notificações pendentes de envio")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<NotificationDTO>> listarNotificacoesPendentes() {
        List<NotificationDTO> pendentes = notificationHistoryService.buscarNotificacoesPendentes();
        return ResponseEntity.ok(pendentes);
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de notificações", 
               description = "Retorna estatísticas sobre as notificações do sistema")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        // Implementar lógica de estatísticas se necessário
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Estatísticas em desenvolvimento");
        
        return ResponseEntity.ok(stats);
    }
}
