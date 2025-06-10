package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Condominio;
import br.com.unit.tokseg.armariointeligente.service.CondominioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/condominios")
@Tag(name = "Condomínios", description = "Endpoints para gerenciamento de condomínios")
public class CondominioController {

    @Autowired
    private CondominioService condominioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar condomínio", description = "Cria um novo condomínio no sistema")
    public ResponseEntity<?> criarCondominio(@RequestBody Condominio condominio) {
        Condominio novoCondominio = condominioService.criarCondominio(condominio);
        return ResponseEntity.ok(novoCondominio);
    }

    @GetMapping
    @Operation(summary = "Listar condomínios", description = "Lista todos os condomínios cadastrados no sistema")
    public ResponseEntity<?> listarCondominios() {
        return ResponseEntity.ok(condominioService.listarCondominios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar condomínio por ID", description = "Busca um condomínio pelo seu ID")
    public ResponseEntity<?> buscarCondominioPorId(
            @Parameter(description = "ID do condomínio") @PathVariable Long id) {
        Optional<Condominio> condominio = condominioService.buscarCondominioPorId(id);
        if (condominio.isPresent()) {
            return ResponseEntity.ok(condominio.get());
        } else {
            throw new ResourceNotFoundException("Condomínio", "id", id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar condomínio", description = "Atualiza os dados de um condomínio existente")
    public ResponseEntity<?> atualizarCondominio(
            @Parameter(description = "ID do condomínio") @PathVariable Long id, 
            @RequestBody Condominio condominio) {
        Condominio condominioAtualizado = condominioService.atualizarCondominio(id, condominio);
        return ResponseEntity.ok(condominioAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar condomínio", description = "Remove um condomínio do sistema")
    public ResponseEntity<?> deletarCondominio(
            @Parameter(description = "ID do condomínio") @PathVariable Long id) {
        condominioService.deletarCondominio(id);
        return ResponseEntity.noContent().build();
    }
}
