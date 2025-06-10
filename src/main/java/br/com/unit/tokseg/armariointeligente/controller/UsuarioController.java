package br.com.unit.tokseg.armariointeligente.controller;

import java.util.List; // Adicionar importação
import java.util.Optional;
import java.util.stream.Collectors; // Adicionar importação

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.TipoUsuario; // Importar TipoUsuario
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.dto.UsuarioDTO;
import br.com.unit.tokseg.armariointeligente.service.UsuarioService;
import br.com.unit.tokseg.armariointeligente.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuditService auditService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema (requer permissão de ADMINISTRADOR)")
    public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        // Converte DTO para entidade Usuario
        Usuario usuario = convertToEntity(usuarioDTO);
        Usuario novoUsuario = usuarioService.criarUsuario(usuario);
        auditService.logUserAction("CREATE", "Usuario", novoUsuario.getId(),
                "Usuário criado: " + novoUsuario.getEmail());
        return ResponseEntity.ok(convertToDTO(novoUsuario));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários cadastrados no sistema")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> dtos = usuarioService.listarUsuarios().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping(value = "/ativos")
    @Operation(summary = "Listar usuários ativos", description = "Lista todos os usuários ativos no sistema")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosAtivos() {
        List<UsuarioDTO> dtos = usuarioService.listarUsuariosAtivos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioService.isCurrentUser(#id)")
    @Operation(summary = "Buscar usuário por ID", description = "Busca um usuário pelo seu ID")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorId(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarUsuarioPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            auditService.logDataAccess("READ", "Usuario", id);
            return ResponseEntity.ok(convertToDTO(usuario));
        } else {
            throw new ResourceNotFoundException("Usuário", "id", id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioService.isCurrentUser(#id)")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        // Converte DTO para entidade Usuario
        Usuario usuario = convertToEntity(usuarioDTO);
        Usuario usuarioAtualizado = usuarioService.atualizarUsuario(id, usuario);
        auditService.logUserAction("UPDATE", "Usuario", id,
                "Usuário atualizado: " + usuarioAtualizado.getEmail());
        return ResponseEntity.ok(convertToDTO(usuarioAtualizado));
    }

    @PutMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário no sistema (requer permissão de ADMINISTRADOR)")
    public ResponseEntity<UsuarioDTO> desativarUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        Usuario usuarioDesativado = usuarioService.desativarUsuario(id);
        auditService.logUserAction("DEACTIVATE", "Usuario", id,
                "Usuário desativado: " + usuarioDesativado.getEmail());
        return ResponseEntity.ok(convertToDTO(usuarioDesativado));
    }

    @PutMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar usuário", description = "Ativa um usuário no sistema (requer permissão de ADMINISTRADOR)")
    public ResponseEntity<UsuarioDTO> ativarUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        Usuario usuarioAtivado = usuarioService.ativarUsuario(id);
        auditService.logUserAction("ACTIVATE", "Usuario", id,
                "Usuário ativado: " + usuarioAtivado.getEmail());
        return ResponseEntity.ok(convertToDTO(usuarioAtivado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema (requer permissão de ADMINISTRADOR)")
    public ResponseEntity<?> deletarUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarUsuarioPorId(id); // Busca para log antes de deletar
        usuarioService.deletarUsuario(id);
        auditService.logUserAction("DELETE", "Usuario", id,
                "Usuário deletado: " + (usuarioOpt.map(Usuario::getEmail).orElse("N/A")));
        return ResponseEntity.noContent().build();
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setTelefone(usuario.getTelefone());
        dto.setAtivo(usuario.isAtivo());
        if (usuario.getTipoUsuario() != null) {
            dto.setTipoUsuarioId(usuario.getTipoUsuario().getId());
            dto.setTipoUsuarioNome(usuario.getTipoUsuario().getNome());
        }
        // A senha não deve ser exposta no DTO de resposta
        return dto;
    }

    private Usuario convertToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        // Não definimos o ID aqui, pois será gerado automaticamente
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setSenha(dto.getSenha()); // A senha será criptografada no service
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

        // Se tipoUsuarioId foi fornecido, cria um TipoUsuario com esse ID
        if (dto.getTipoUsuarioId() != null) {
            TipoUsuario tipoUsuario = new TipoUsuario();
            tipoUsuario.setId(dto.getTipoUsuarioId());
            usuario.setTipoUsuario(tipoUsuario);
        }

        return usuario;
    }
}
