package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CacheServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CacheManager cacheManager;

    private TipoUsuario tipoUsuario;

    @BeforeEach
    public void setup() {
        // Limpar cache
        cacheManager.getCacheNames().forEach(cacheName -> 
            cacheManager.getCache(cacheName).clear());

        // Limpar dados
        usuarioRepository.deleteAll();
        tipoUsuarioRepository.deleteAll();

        // Criar tipo de usuário
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setNome("Cliente");
        tipoUsuario.setDescricao("Usuário cliente");
        tipoUsuario = tipoUsuarioRepository.save(tipoUsuario);
    }

    @Test
    public void testCacheUsuarios_ListarUsuarios() {
        // Criar usuário
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);
        usuarioRepository.save(usuario);

        // Primeira chamada - deve buscar no banco
        List<Usuario> usuarios1 = usuarioService.listarUsuarios();
        assertNotNull(usuarios1);
        assertEquals(1, usuarios1.size());

        // Verificar se foi cacheado
        assertNotNull(cacheManager.getCache("usuarios"));

        // Segunda chamada - deve vir do cache
        List<Usuario> usuarios2 = usuarioService.listarUsuarios();
        assertNotNull(usuarios2);
        assertEquals(1, usuarios2.size());
        assertEquals(usuarios1.get(0).getId(), usuarios2.get(0).getId());
    }

    @Test
    public void testCacheInvalidation_AtualizarUsuario() {
        // Criar usuário
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);
        usuario = usuarioRepository.save(usuario);

        // Cachear dados
        usuarioService.listarUsuarios();

        // Atualizar usuário - deve invalidar cache
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Santos");
        usuarioService.atualizarUsuario(usuario.getId(), usuarioAtualizado);

        // Verificar se cache foi invalidado
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        assertEquals("João Santos", usuarios.get(0).getNome());
    }

    @Test
    public void testCacheBuscarUsuarioPorId() {
        // Criar usuário
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);
        usuario = usuarioRepository.save(usuario);

        // Primeira busca - deve buscar no banco
        var resultado1 = usuarioService.buscarUsuarioPorId(usuario.getId());
        assertTrue(resultado1.isPresent());

        // Segunda busca - deve vir do cache
        var resultado2 = usuarioService.buscarUsuarioPorId(usuario.getId());
        assertTrue(resultado2.isPresent());
        assertEquals(resultado1.get().getId(), resultado2.get().getId());
    }
}
