package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Compartimento;
import br.com.unit.tokseg.armariointeligente.service.CompartimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/compartimentos")
@Tag(name = "Compartimentos", description = "Endpoints para gerenciamento de compartimentos")
public class CompartimentoController {

    @Autowired
    private CompartimentoService compartimentoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar compartimento", description = "Cria um novo compartimento no sistema")
    public ResponseEntity<?> criarCompartimento(@RequestBody Compartimento compartimento) {
        Compartimento novoCompartimento = compartimentoService.criarCompartimento(compartimento);
        return ResponseEntity.ok(novoCompartimento);
    }

    @GetMapping
    @Operation(summary = "Listar compartimentos", description = "Lista todos os compartimentos cadastrados no sistema")
    public ResponseEntity<?> listarCompartimentos() {
        return ResponseEntity.ok(compartimentoService.listarCompartimentos());
    }

    @GetMapping("/armario/{armarioId}")
    @Operation(summary = "Listar compartimentos por armário", description = "Lista todos os compartimentos de um armário específico")
    public ResponseEntity<?> listarCompartimentosPorArmario(
            @Parameter(description = "ID do armário") @PathVariable Long armarioId) {
        return ResponseEntity.ok(compartimentoService.listarCompartimentosPorArmario(armarioId));
    }

    @GetMapping("/status")
    @Operation(summary = "Listar compartimentos por status", description = "Lista compartimentos filtrados por status (ocupado/livre)")
    public ResponseEntity<?> listarCompartimentosPorStatus(
            @Parameter(description = "Status de ocupação (true/false)") @RequestParam Boolean ocupado) {
        return ResponseEntity.ok(compartimentoService.listarCompartimentosPorStatus(ocupado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar compartimento por ID", description = "Busca um compartimento pelo seu ID")
    public ResponseEntity<?> buscarCompartimentoPorId(
            @Parameter(description = "ID do compartimento") @PathVariable Long id) {
        Optional<Compartimento> compartimento = compartimentoService.buscarCompartimentoPorId(id);
        if (compartimento.isPresent()) {
            return ResponseEntity.ok(compartimento.get());
        } else {
            throw new ResourceNotFoundException("Compartimento", "id", id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar compartimento", description = "Atualiza os dados de um compartimento existente")
    public ResponseEntity<?> atualizarCompartimento(
            @Parameter(description = "ID do compartimento") @PathVariable Long id, 
            @RequestBody Compartimento compartimento) {
        Compartimento compartimentoAtualizado = compartimentoService.atualizarCompartimento(id, compartimento);
        return ResponseEntity.ok(compartimentoAtualizado);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ENTREGADOR')")
    @Operation(summary = "Atualizar status do compartimento", description = "Atualiza o status de ocupação de um compartimento")
    public ResponseEntity<?> atualizarStatusCompartimento(
            @Parameter(description = "ID do compartimento") @PathVariable Long id, 
            @Parameter(description = "Status de ocupação (true/false)") @RequestParam Boolean ocupado) {
        Compartimento compartimentoAtualizado = compartimentoService.atualizarStatusCompartimento(id, ocupado);
        return ResponseEntity.ok(compartimentoAtualizado);
    }

    @PutMapping("/{id}/codigo-acesso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Gerar novo código de acesso", description = "Gera um novo código de acesso para o compartimento")
    public ResponseEntity<?> gerarNovoCodigoAcesso(
            @Parameter(description = "ID do compartimento") @PathVariable Long id) {
        Compartimento compartimentoAtualizado = compartimentoService.gerarNovoCodigoAcesso(id);
        return ResponseEntity.ok(compartimentoAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar compartimento", description = "Remove um compartimento do sistema")
    public ResponseEntity<?> deletarCompartimento(
            @Parameter(description = "ID do compartimento") @PathVariable Long id) {
        compartimentoService.deletarCompartimento(id);
        return ResponseEntity.noContent().build();
    }
}
