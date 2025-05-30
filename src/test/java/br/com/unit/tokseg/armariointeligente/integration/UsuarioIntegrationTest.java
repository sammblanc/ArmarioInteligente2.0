package br.com.unit.tokseg.armariointeligente.integration;

import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.repository.TipoUsuarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testCriarUsuario_FluxoCompleto() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.ativo").value(true));

        // Verificar se foi salvo no banco
        assert usuarioRepository.findByEmail("joao@example.com").isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testCriarUsuario_EmailDuplicado() throws Exception {
        // Criar primeiro usuário
        Usuario usuario1 = new Usuario();
        usuario1.setNome("João Silva");
        usuario1.setEmail("joao@example.com");
        usuario1.setSenha("senha123");
        usuario1.setTelefone("81999998888");
        usuario1.setTipoUsuario(tipoUsuario);
        usuarioRepository.save(usuario1);

        // Tentar criar segundo usuário com mesmo email
        Usuario usuario2 = new Usuario();
        usuario2.setNome("João Santos");
        usuario2.setEmail("joao@example.com");
        usuario2.setSenha("senha456");
        usuario2.setTelefone("81999997777");
        usuario2.setTipoUsuario(tipoUsuario);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario2)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    public void testListarUsuarios_ComDados() throws Exception {
        // Criar usuários de teste
        Usuario usuario1 = new Usuario();
        usuario1.setNome("João Silva");
        usuario1.setEmail("joao@example.com");
        usuario1.setSenha("senha123");
        usuario1.setTelefone("81999998888");
        usuario1.setTipoUsuario(tipoUsuario);
        usuario1.setAtivo(true);
        usuarioRepository.save(usuario1);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("Maria Santos");
        usuario2.setEmail("maria@example.com");
        usuario2.setSenha("senha456");
        usuario2.setTelefone("81999997777");
        usuario2.setTipoUsuario(tipoUsuario);
        usuario2.setAtivo(true);
        usuarioRepository.save(usuario2);

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
