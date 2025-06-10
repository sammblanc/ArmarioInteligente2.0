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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class EntregaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntregaRepository entregaRepository;

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

    private Entrega entrega;
    private Compartimento compartimento;
    private Usuario entregador;
    private Usuario destinatario;

    @BeforeEach
    public void setup() {
        // Limpar dados
        entregaRepository.deleteAll();
        compartimentoRepository.deleteAll();
        armarioRepository.deleteAll();
        condominioRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoUsuarioRepository.deleteAll();

        // Criar estrutura completa
        TipoUsuario tipoEntregador = new TipoUsuario();
        tipoEntregador.setNome("Entregador");
        tipoEntregador = tipoUsuarioRepository.save(tipoEntregador);

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

        entregador = new Usuario();
        entregador.setNome("Entregador Teste");
        entregador.setEmail("entregador@test.com");
        entregador.setSenha("senha123");
        entregador.setTelefone("81999998888");
        entregador.setTipoUsuario(tipoEntregador);
        entregador = usuarioRepository.save(entregador);

        destinatario = new Usuario();
        destinatario.setNome("Cliente Teste");
        destinatario.setEmail("cliente@test.com");
        destinatario.setSenha("senha123");
        destinatario.setTelefone("81999997777");
        destinatario.setTipoUsuario(tipoCliente);
        destinatario = usuarioRepository.save(destinatario);
    }

    @Test
    @WithMockUser(roles = "ENTREGADOR")
    public void testRegistrarEntrega_FluxoCompleto() throws Exception {
        Entrega entrega = new Entrega();
        entrega.setCodigoRastreio("BR123456789");
        entrega.setDescricao("Pacote teste");
        entrega.setCompartimento(compartimento);
        entrega.setEntregador(entregador);
        entrega.setDestinatario(destinatario);

        mockMvc.perform(post("/api/v1/entregas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entrega)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreio").value("BR123456789"))
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        // Verificar se foi salvo no banco
        assert entregaRepository.findByCodigoRastreio("BR123456789").isPresent();
        
        // Verificar se compartimento foi marcado como ocupado
        Compartimento comp = compartimentoRepository.findById(compartimento.getId()).get();
        assert comp.getOcupado();
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    public void testRegistrarRetirada_FluxoCompleto() throws Exception {
        // Criar entrega primeiro
        entrega = new Entrega();
        entrega.setCodigoRastreio("BR123456789");
        entrega.setDescricao("Pacote teste");
        entrega.setStatus(StatusEntrega.ENTREGUE);
        entrega.setCompartimento(compartimento);
        entrega.setEntregador(entregador);
        entrega.setDestinatario(destinatario);
        entrega = entregaRepository.save(entrega);

        // Marcar compartimento como ocupado
        compartimento.setOcupado(true);
        compartimentoRepository.save(compartimento);

        mockMvc.perform(put("/api/v1/entregas/" + entrega.getId() + "/retirada")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRADO"))
                .andExpect(jsonPath("$.dataRetirada").exists());

        // Verificar se compartimento foi liberado
        Compartimento comp = compartimentoRepository.findById(compartimento.getId()).get();
        assert !comp.getOcupado();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testListarEntregas() throws Exception {
        // Criar entrega
        entrega = new Entrega();
        entrega.setCodigoRastreio("BR123456789");
        entrega.setDescricao("Pacote teste");
        entrega.setStatus(StatusEntrega.ENTREGUE);
        entrega.setCompartimento(compartimento);
        entrega.setEntregador(entregador);
        entrega.setDestinatario(destinatario);
        entregaRepository.save(entrega);

        mockMvc.perform(get("/api/v1/entregas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigoRastreio").value("BR123456789"));
    }
}
