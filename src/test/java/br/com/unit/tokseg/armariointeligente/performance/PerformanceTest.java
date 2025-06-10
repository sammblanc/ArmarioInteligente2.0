package br.com.unit.tokseg.armariointeligente.performance;

import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.service.UsuarioService;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PerformanceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    private TipoUsuario tipoUsuario;

    @BeforeEach
    public void setup() {
        usuarioRepository.deleteAll();
        tipoUsuarioRepository.deleteAll();

        tipoUsuario = new TipoUsuario();
        tipoUsuario.setNome("Cliente");
        tipoUsuario.setDescricao("Usuário cliente");
        tipoUsuario = tipoUsuarioRepository.save(tipoUsuario);
    }

    @Test
    public void testPerformanceCriacaoUsuarios() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Criar 100 usuários
        for (int i = 0; i < 100; i++) {
            Usuario usuario = new Usuario();
            usuario.setNome("Usuario " + i);
            usuario.setEmail("usuario" + i + "@test.com");
            usuario.setSenha("senha123");
            usuario.setTelefone("8199999" + String.format("%04d", i));
            usuario.setTipoUsuario(tipoUsuario);
            usuarioService.criarUsuario(usuario);
        }

        stopWatch.stop();
        long tempoExecucao = stopWatch.getTotalTimeMillis();
        
        System.out.println("Tempo para criar 100 usuários: " + tempoExecucao + "ms");
        assertTrue(tempoExecucao < 10000, "Criação de 100 usuários deve levar menos de 10 segundos");
    }

    @Test
    public void testPerformanceListagemUsuarios() {
        // Criar alguns usuários primeiro
        for (int i = 0; i < 50; i++) {
            Usuario usuario = new Usuario();
            usuario.setNome("Usuario " + i);
            usuario.setEmail("usuario" + i + "@test.com");
            usuario.setSenha("senha123");
            usuario.setTelefone("8199999" + String.format("%04d", i));
            usuario.setTipoUsuario(tipoUsuario);
            usuarioService.criarUsuario(usuario);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Executar listagem 10 vezes
        for (int i = 0; i < 10; i++) {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            assertNotNull(usuarios);
        }

        stopWatch.stop();
        long tempoExecucao = stopWatch.getTotalTimeMillis();
        
        System.out.println("Tempo para 10 listagens de usuários: " + tempoExecucao + "ms");
        assertTrue(tempoExecucao < 5000, "10 listagens devem levar menos de 5 segundos");
    }

    @Test
    public void testPerformanceCacheUsuarios() {
        // Criar usuários
        for (int i = 0; i < 20; i++) {
            Usuario usuario = new Usuario();
            usuario.setNome("Usuario " + i);
            usuario.setEmail("usuario" + i + "@test.com");
            usuario.setSenha("senha123");
            usuario.setTelefone("8199999" + String.format("%04d", i));
            usuario.setTipoUsuario(tipoUsuario);
            usuarioService.criarUsuario(usuario);
        }

        // Primeira listagem (sem cache)
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        usuarioService.listarUsuarios();
        stopWatch1.stop();
        long tempo1 = stopWatch1.getTotalTimeMillis();

        // Segunda listagem (com cache)
        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        usuarioService.listarUsuarios();
        stopWatch2.stop();
        long tempo2 = stopWatch2.getTotalTimeMillis();

        System.out.println("Primeira listagem (sem cache): " + tempo1 + "ms");
        System.out.println("Segunda listagem (com cache): " + tempo2 + "ms");
        
        // Cache deve melhorar a performance
        assertTrue(tempo2 <= tempo1, "Cache deve melhorar ou manter a performance");
    }

    @Test
    public void testPerformanceConcorrencia() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Criar 50 usuários concorrentemente
        for (int i = 0; i < 50; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Usuario usuario = new Usuario();
                usuario.setNome("Usuario Concorrente " + index);
                usuario.setEmail("concorrente" + index + "@test.com");
                usuario.setSenha("senha123");
                usuario.setTelefone("8199999" + String.format("%04d", index));
                usuario.setTipoUsuario(tipoUsuario);
                usuarioService.criarUsuario(usuario);
            }, executor);
            futures.add(future);
        }

        // Aguardar todas as operações
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        stopWatch.stop();
        long tempoExecucao = stopWatch.getTotalTimeMillis();
        
        System.out.println("Tempo para criar 50 usuários concorrentemente: " + tempoExecucao + "ms");
        assertTrue(tempoExecucao < 15000, "Criação concorrente deve levar menos de 15 segundos");

        // Verificar se todos foram criados
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        assertEquals(50, usuarios.size());

        executor.shutdown();
    }
}
