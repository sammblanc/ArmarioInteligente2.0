package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.model.TipoUsuario;
import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private TipoUsuario tipoUsuario;

    @BeforeEach
    public void setup() {
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setId(1L);
        tipoUsuario.setNome("Cliente");

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
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testCriarUsuario_Sucesso() throws Exception {
        when(usuarioService.criarUsuario(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));

        verify(usuarioService, times(1)).criarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    public void testCriarUsuario_SemPermissao() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).criarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser
    public void testListarUsuarios_Sucesso() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(Arrays.asList(usuario));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpected(jsonPath("$[0].nome").value("João Silva"));

        verify(usuarioService, times(1)).listarUsuarios();
    }

    @Test
    @WithMockUser(username = "joao@example.com")
    public void testBuscarUsuarioPorId_Sucesso() throws Exception {
        when(usuarioService.buscarUsuarioPorId(anyLong())).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(usuarioService, times(1)).buscarUsuarioPorId(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testAtualizarUsuario_Sucesso() throws Exception {
        when(usuarioService.atualizarUsuario(anyLong(), any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/v1/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(usuarioService, times(1)).atualizarUsuario(anyLong(), any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testDesativarUsuario_Sucesso() throws Exception {
        usuario.setAtivo(false);
        when(usuarioService.desativarUsuario(anyLong())).thenReturn(usuario);

        mockMvc.perform(put("/api/v1/usuarios/1/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        verify(usuarioService, times(1)).desativarUsuario(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testDeletarUsuario_Sucesso() throws Exception {
        doNothing().when(usuarioService).deletarUsuario(anyLong());

        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).deletarUsuario(anyLong());
    }
}
