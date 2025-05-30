package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private TipoUsuario tipoUsuario;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        tipoUsuario = new TipoUsuario();
        tipoUsuario.setId(1L);
        tipoUsuario.setNome("Cliente");
        tipoUsuario.setDescricao("Usuário cliente");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setAtivo(true);
    }

    @Test
    public void testCriarUsuario_Sucesso() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(tipoUsuarioRepository.findById(anyLong())).thenReturn(Optional.of(tipoUsuario));
        when(passwordEncoder.encode(anyString())).thenReturn("senhaEncriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.criarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals(usuario.getEmail(), resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    public void testCriarUsuario_EmailJaExiste() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            usuarioService.criarUsuario(usuario);
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    public void testCriarUsuario_UsuarioNulo() {
        assertThrows(BadRequestException.class, () -> {
            usuarioService.criarUsuario(null);
        });
    }

    @Test
    public void testCriarUsuario_EmailNulo() {
        usuario.setEmail(null);

        assertThrows(BadRequestException.class, () -> {
            usuarioService.criarUsuario(usuario);
        });
    }

    @Test
    public void testCriarUsuario_SenhaNula() {
        usuario.setSenha(null);

        assertThrows(BadRequestException.class, () -> {
            usuarioService.criarUsuario(usuario);
        });
    }

    @Test
    public void testCriarUsuario_TipoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(tipoUsuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.criarUsuario(usuario);
        });
    }

    @Test
    public void testListarUsuarios() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarUsuarioPorId_Encontrado() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarUsuarioPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(usuario.getId(), resultado.get().getId());
    }

    @Test
    public void testBuscarUsuarioPorId_NaoEncontrado() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.buscarUsuarioPorId(1L);

        assertFalse(resultado.isPresent());
    }

    @Test
    public void testAtualizarUsuario_Sucesso() {
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Santos");
        usuarioAtualizado.setEmail("joao.santos@example.com");

        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizarUsuario(1L, usuarioAtualizado);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    public void testAtualizarUsuario_UsuarioNaoEncontrado() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuario);
        });
    }

    @Test
    public void testDesativarUsuario() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.desativarUsuario(1L);

        assertNotNull(resultado);
        assertFalse(resultado.isAtivo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    public void testAtivarUsuario() {
        usuario.setAtivo(false);
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.ativarUsuario(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isAtivo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    public void testDeletarUsuario_Sucesso() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(true);

        assertDoesNotThrow(() -> {
            usuarioService.deletarUsuario(1L);
        });

        verify(usuarioRepository, times(1)).deleteById(anyLong());
    }

    @Test
    public void testDeletarUsuario_UsuarioNaoEncontrado() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.deletarUsuario(1L);
        });

        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}
