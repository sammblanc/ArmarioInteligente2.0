package br.com.unit.tokseg.armariointeligente.integration;

import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.*;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class ReservaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CompartimentoRepository compartimentoRepository;

    @Autowired
    private ArmarioRepository armarioRepository;

    @Autowired
    private CondominioRepository condominioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Compartimento compartimento;
    private Usuario usuario;

    @BeforeEach
    public void setup() {
        // Limpar dados
        reservaRepository.deleteAll();
        compartimentoRepository.deleteAll();
        armarioRepository.deleteAll();
        condominioRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoUsuarioRepository.deleteAll();

        // Criar estrutura completa
        TipoUsuario tipoCliente = new TipoUsuario();
        tipoCliente.setNome("Cliente");
        tipoCliente = tipoUsuarioRepository.save(tipoCliente);

        Condominio condominio = new Condominio();
        condominio.setNome("Condomínio Teste");
        condominio.setEndereco("Rua Teste, 123");
        condominio = condominioRepository.save(condominio);

        Armario armario = new Armario();
        armario.setIdentificacao("ARM-001");
        armario.setLocalizacao("Hall");
        armario.setCondominio(condominio);
        armario = armarioRepository.save(armario);

        compartimento = new Compartimento();
        compartimento.setNumero("A1");
        compartimento.setTamanho("M");
        compartimento.setOcupado(false);
        compartimento.setCodigoAcesso("123456");
        compartimento.setArmario(armario);
        compartimento = compartimentoRepository.save(compartimento);

        usuario = new Usuario();
        usuario.setNome("Cliente Teste");
        usuario.setEmail("cliente@test.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999997777");
        usuario.setTipoUsuario(tipoCliente);
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    public void testCriarReserva_FluxoCompleto() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setDataInicio(LocalDateTime.now().plusHours(1));
        reserva.setDataFim(LocalDateTime.now().plusHours(3));
        reserva.setObservacao("Reserva teste");
        reserva.setCompartimento(compartimento);
        reserva.setUsuario(usuario);

        mockMvc.perform(post("/api/v1/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.observacao").value("Reserva teste"));

        // Verificar se compartimento foi marcado como ocupado
        Compartimento comp = compartimentoRepository.findById(compartimento.getId()).get();
        assert comp.getOcupado();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testCancelarReserva_FluxoCompleto() throws Exception {
        // Criar reserva primeiro
        Reserva reserva = new Reserva();
        reserva.setDataInicio(LocalDateTime.now().plusHours(1));
        reserva.setDataFim(LocalDateTime.now().plusHours(3));
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setCompartimento(compartimento);
        reserva.setUsuario(usuario);
        reserva = reservaRepository.save(reserva);

        // Marcar compartimento como ocupado
        compartimento.setOcupado(true);
        compartimentoRepository.save(compartimento);

        mockMvc.perform(put("/api/v1/reservas/" + reserva.getId() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        // Verificar se compartimento foi liberado
        Compartimento comp = compartimentoRepository.findById(compartimento.getId()).get();
        assert !comp.getOcupado();
    }
}
