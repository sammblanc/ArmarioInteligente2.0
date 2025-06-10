package br.com.unit.tokseg.armariointeligente.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.service.TipoUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/v1/tipos-usuarios")
@Tag(name = "Tipos de Usuário", description = "Endpoints para gerenciamento de tipos de usuário")
public class TipoUsuarioController {

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar tipo de usuário", description = "Cria um novo tipo de usuário no sistema")
    public ResponseEntity<?> criarTipoUsuario(@RequestBody TipoUsuario tipoUsuario) {
        TipoUsuario novoTipoUsuario = tipoUsuarioService.criarTipoUsuario(tipoUsuario);
        return ResponseEntity.ok(novoTipoUsuario);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de usuário", description = "Lista todos os tipos de usuário cadastrados no sistema")
    public ResponseEntity<?> listarTiposUsuarios() {
        return ResponseEntity.ok(tipoUsuarioService.listarTiposUsuarios());
    }


    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de usuário por ID", description = "Busca um tipo de usuário pelo seu ID")
    public ResponseEntity<?> buscarTipoUsuarioPorId(
            @Parameter(description = "ID do tipo de usuário") @PathVariable Long id) {
        Optional<TipoUsuario> tipoUsuario = tipoUsuarioService.buscarTipoUsuarioPorId(id);
        if (tipoUsuario.isPresent()) {
            return ResponseEntity.ok(tipoUsuario.get());
        } else {
            throw new ResourceNotFoundException("Tipo de usuário", "id", id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar tipo de usuário", description = "Atualiza os dados de um tipo de usuário existente")
    public ResponseEntity<?> atualizarTipoUsuario(
            @Parameter(description = "ID do tipo de usuário") @PathVariable Long id,
            @RequestBody TipoUsuario tipoUsuario) {
        TipoUsuario tipoAtualizado = tipoUsuarioService.atualizarTipoUsuario(id, tipoUsuario);
        return ResponseEntity.ok(tipoAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar tipo de usuário", description = "Remove um tipo de usuário do sistema")
    public ResponseEntity<?> deletarTipoUsuario(
            @Parameter(description = "ID do tipo de usuário") @PathVariable Long id) {
        tipoUsuarioService.deletarTipoUsuario(id);
        return ResponseEntity.noContent().build();
    }

}
