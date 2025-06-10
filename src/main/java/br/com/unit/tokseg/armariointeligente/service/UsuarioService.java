package br.com.unit.tokseg.armariointeligente.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.unit.tokseg.armariointeligente.security.UserDetailsImpl;
import br.com.unit.tokseg.armariointeligente.util.ValidationUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new BadRequestException("Usuário não pode ser nulo");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            throw new BadRequestException("Email não pode ser nulo ou vazio");
        }
        if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
            throw new BadRequestException("Senha não pode ser nula ou vazia");
        }

        // Validações adicionais
        ValidationUtils.validateNotBlank(usuario.getNome(), "Nome");
        ValidationUtils.validateStringLength(usuario.getNome(), "Nome", 2, 255);
        ValidationUtils.validateEmail(usuario.getEmail());
        ValidationUtils.validateStringLength(usuario.getEmail(), "Email", 1, 255);
        ValidationUtils.validateStringLength(usuario.getSenha(), "Senha", 6, 255);
        ValidationUtils.validateNotBlank(usuario.getTelefone(), "Telefone");
        ValidationUtils.validatePhone(usuario.getTelefone());

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Usuário", "email", usuario.getEmail());
        }

        if (usuario.getTipoUsuario() == null || usuario.getTipoUsuario().getId() == null) {
            throw new BadRequestException("Tipo de usuário é obrigatório");
        }

        TipoUsuario tipoUsuario = tipoUsuarioRepository.findById(usuario.getTipoUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário", "id",
                        usuario.getTipoUsuario().getId()));

        // Criptografar a senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Cacheable(value = "usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
    
    @Transactional
    public List<Usuario> listarUsuariosAtivos() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isAtivo)
                .collect(Collectors.toList());
    }

    @Transactional
    @Cacheable(value = "usuarios", key = "#id")
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario atualizarUsuario(Long id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        // Validações adicionais para campos atualizados
        if (usuario.getNome() != null) {
            ValidationUtils.validateNotBlank(usuario.getNome(), "Nome");
            ValidationUtils.validateStringLength(usuario.getNome(), "Nome", 2, 255);
        }
        if (usuario.getEmail() != null) {
            ValidationUtils.validateEmail(usuario.getEmail());
            ValidationUtils.validateStringLength(usuario.getEmail(), "Email", 1, 255);
        }
        if (usuario.getSenha() != null) {
            ValidationUtils.validateStringLength(usuario.getSenha(), "Senha", 6, 255);
        }
        if (usuario.getTelefone() != null) {
            ValidationUtils.validateNotBlank(usuario.getTelefone(), "Telefone");
            ValidationUtils.validatePhone(usuario.getTelefone());
        }

        // Verifica se o email já está em uso por outro usuário
        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
            Optional<Usuario> usuarioComMesmoEmail =
                    usuarioRepository.findByEmail(usuario.getEmail());
            if (usuarioComMesmoEmail.isPresent()
                    && !usuarioComMesmoEmail.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Usuário", "email", usuario.getEmail());
            }
            usuarioExistente.setEmail(usuario.getEmail());
        }

        // Atualiza o tipo de usuário se fornecido
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() != null) {
            TipoUsuario tipoUsuario =
                    tipoUsuarioRepository.findById(usuario.getTipoUsuario().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário",
                                    "id", usuario.getTipoUsuario().getId()));
            usuarioExistente.setTipoUsuario(tipoUsuario);
        }

        // Atualiza outros campos se fornecidos
        if (usuario.getNome() != null && !usuario.getNome().isEmpty()) {
            usuarioExistente.setNome(usuario.getNome());
        }

        if (usuario.getTelefone() != null && !usuario.getTelefone().isEmpty()) {
            usuarioExistente.setTelefone(usuario.getTelefone());
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            // Criptografar a senha
            usuarioExistente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return usuarioRepository.save(usuarioExistente);
    }
    
    @Transactional
    public Usuario desativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }
    
    @Transactional
    public Usuario ativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", "id", id);
        }
        usuarioRepository.deleteById(id);
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId().equals(userId);
        }
        
        return false;
    }
}
