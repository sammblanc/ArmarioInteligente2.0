package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Armario;
import br.com.unit.tokseg.armariointeligente.service.ArmarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.unit.tokseg.armariointeligente.dto.ArmarioDTO;
import br.com.unit.tokseg.armariointeligente.model.Condominio;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/armarios")
@Tag(name = "Armários", description = "Endpoints para gerenciamento de armários") // Adicionada anotação Tag que faltava
public class ArmarioController {

    @Autowired
    private ArmarioService armarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar armário", description = "Cria um novo armário no sistema")
    public ResponseEntity<?> criarArmario(@RequestBody ArmarioDTO armarioDTO) {
        // Converte o DTO para a entidade Armario
        Armario armario = new Armario();
        armario.setIdentificacao(armarioDTO.getIdentificacao());
        armario.setLocalizacao(armarioDTO.getLocalizacao());
        armario.setDescricao(armarioDTO.getDescricao());
        armario.setAtivo(armarioDTO.getAtivo());

        if (armarioDTO.getCondominioId() != null) {
            Condominio condominio = new Condominio();
            condominio.setId(armarioDTO.getCondominioId());
            armario.setCondominio(condominio);
        }


        Armario novoArmario = armarioService.criarArmario(armario);
        return ResponseEntity.ok(novoArmario);
    }

    @GetMapping
    @Operation(summary = "Listar armários", description = "Lista todos os armários cadastrados no sistema")
    public ResponseEntity<?> listarArmarios() {

        return ResponseEntity.ok(armarioService.listarArmarios());
    }

    @GetMapping("/condominio/{condominioId}")
    @Operation(summary = "Listar armários por condomínio", description = "Lista todos os armários de um condomínio específico")
    public ResponseEntity<?> listarArmariosPorCondominio(
            @Parameter(description = "ID do condomínio") @PathVariable Long condominioId) {

        return ResponseEntity.ok(armarioService.listarArmariosPorCondominio(condominioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar armário por ID", description = "Busca um armário pelo seu ID")
    public ResponseEntity<?> buscarArmarioPorId(
            @Parameter(description = "ID do armário") @PathVariable Long id) {

        Optional<Armario> armario = armarioService.buscarArmarioPorId(id);
        if (armario.isPresent()) {
            return ResponseEntity.ok(armario.get());
        } else {
            throw new ResourceNotFoundException("Armário", "id", id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar armário", description = "Atualiza os dados de um armário existente")
    public ResponseEntity<?> atualizarArmario(
            @Parameter(description = "ID do armário") @PathVariable Long id,
            @RequestBody Armario armario) {

        Armario armarioAtualizado = armarioService.atualizarArmario(id, armario);
        return ResponseEntity.ok(armarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar armário", description = "Remove um armário do sistema")
    public ResponseEntity<?> deletarArmario(
            @Parameter(description = "ID do armário") @PathVariable Long id) {

        armarioService.deletarArmario(id);
        return ResponseEntity.noContent().build();
    }
}