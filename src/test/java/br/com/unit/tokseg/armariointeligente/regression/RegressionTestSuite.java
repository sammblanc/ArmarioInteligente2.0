package br.com.unit.tokseg.armariointeligente.regression;

import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.*;
import br.com.unit.tokseg.armariointeligente.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RegressionTestSuite {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private EntregaService entregaService;
    
    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private CompartimentoService compartimentoService;
    
    @Autowired
    private CondominioService condominioService;
    
    @Autowired
    private ArmarioService armarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;
    
    @Autowired
    private CondominioRepository condominioRepository;
    
    @Autowired
    private ArmarioRepository armarioRepository;
    
    @Autowired
    private CompartimentoRepository compartimentoRepository;
    
    @Autowired
    private EntregaRepository entregaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TipoUsuario tipoUsuario;
    private Usuario usuario;
    private Condominio condominio;
    private Armario armario;
    private Compartimento compartimento;

    @BeforeEach
    public void setup() {
        // Limpar todos os dados
        entregaRepository.deleteAll();
        reservaRepository.deleteAll();
        compartimentoRepository.deleteAll();
        armarioRepository.deleteAll();
        condominioRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoUsuarioRepository.deleteAll();

        // Criar estrutura básica
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setNome("Cliente");
        tipoUsuario.setDescricao("Usuário cliente");
        tipoUsuario = tipoUsuarioRepository.save(tipoUsuario);

        usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("81999998888");
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setAtivo(true);

        condominio = new Condominio();
        condominio.setNome("Condomínio Teste");
        condominio.setEndereco("Rua Teste, 123");
        condominio.setCep("50000-000");
        condominio.setCidade("Recife");
        condominio.setEstado("PE");

        armario = new Armario();
        armario.setIdentificacao("ARM-001");
        armario.setLocalizacao("Hall");
        armario.setAtivo(true);

        compartimento = new Compartimento();
        compartimento.setNumero("A1");
        compartimento.setTamanho("M");
        compartimento.setOcupado(false);
        compartimento.setCodigoAcesso("123456");
    }

    @Test
    public void testFluxoCompletoUsuario_Regressao() {
        // Teste de regressão: fluxo completo de usuário
        
        // 1. Criar usuário
        Usuario novoUsuario = usuarioService.criarUsuario(usuario);
        assertNotNull(novoUsuario.getId());
        assertTrue(novoUsuario.isAtivo());
        assertTrue(passwordEncoder.matches("senha123", novoUsuario.getSenha()));

        // 2. Buscar usuário
        var usuarioEncontrado = usuarioService.buscarUsuarioPorId(novoUsuario.getId());
        assertTrue(usuarioEncontrado.isPresent());
        assertEquals("João Silva", usuarioEncontrado.get().getNome());

        // 3. Atualizar usuário
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("João Santos");
        Usuario resultado = usuarioService.atualizarUsuario(novoUsuario.getId(), usuarioAtualizado);
        assertEquals("João Santos", resultado.getNome());

        // 4. Desativar usuário
        Usuario usuarioDesativado = usuarioService.desativarUsuario(novoUsuario.getId());
        assertFalse(usuarioDesativado.isAtivo());

        // 5. Ativar usuário
        Usuario usuarioReativado = usuarioService.ativarUsuario(novoUsuario.getId());
        assertTrue(usuarioReativado.isAtivo());
    }

    @Test
    public void testFluxoCompletoCondominio_Regressao() {
        // Teste de regressão: fluxo completo de condomínio
        
        // 1. Criar condomínio
        Condominio novoCondominio = condominioService.criarCondominio(condominio);
        assertNotNull(novoCondominio.getId());
        assertEquals("Condomínio Teste", novoCondominio.getNome());

        // 2. Buscar condomínio
        var condominioEncontrado = condominioService.buscarCondominioPorId(novoCondominio.getId());
        assertTrue(condominioEncontrado.isPresent());

        // 3. Atualizar condomínio
        Condominio condominioAtualizado = new Condominio();
        condominioAtualizado.setNome("Condomínio Atualizado");
        Condominio resultado = condominioService.atualizarCondominio(novoCondominio.getId(), condominioAtualizado);
        assertEquals("Condomínio Atualizado", resultado.getNome());
    }

    @Test
    public void testFluxoCompletoArmarioCompartimento_Regressao() {
        // Teste de regressão: fluxo completo de armário e compartimento
        
        // 1. Criar condomínio primeiro
        Condominio novoCondominio = condominioService.criarCondominio(condominio);
        armario.setCondominio(novoCondominio);

        // 2. Criar armário
        Armario novoArmario = armarioService.criarArmario(armario);
        assertNotNull(novoArmario.getId());
        assertEquals("ARM-001", novoArmario.getIdentificacao());

        // 3. Criar compartimento
        compartimento.setArmario(novoArmario);
        Compartimento novoCompartimento = compartimentoService.criarCompartimento(compartimento);
        assertNotNull(novoCompartimento.getId());
        assertEquals("A1", novoCompartimento.getNumero());
        assertFalse(novoCompartimento.getOcupado());

        // 4. Atualizar status do compartimento
        Compartimento compartimentoOcupado = compartimentoService.atualizarStatusCompartimento(
            novoCompartimento.getId(), true);
        assertTrue(compartimentoOcupado.getOcupado());

        // 5. Gerar novo código de acesso
        Compartimento compartimentoNovocodigo = compartimentoService.gerarNovoCodigoAcesso(
            novoCompartimento.getId());
        assertNotEquals("123456", compartimentoNovocodigo.getCodigoAcesso());
    }

    @Test
    public void testFluxoCompletoEntrega_Regressao() {
        // Teste de regressão: fluxo completo de entrega
        
        // Setup completo
        Condominio novoCondominio = condominioService.criarCondominio(condominio);
        armario.setCondominio(novoCondominio);
        Armario novoArmario = armarioService.criarArmario(armario);
        compartimento.setArmario(novoArmario);
        Compartimento novoCompartimento = compartimentoService.criarCompartimento(compartimento);
        
        // Criar usuários
        Usuario novoUsuario = usuarioService.criarUsuario(usuario);
        
        TipoUsuario tipoEntregador = new TipoUsuario();
        tipoEntregador.setNome("Entregador");
        tipoEntregador = tipoUsuarioRepository.save(tipoEntregador);
        
        Usuario entregador = new Usuario();
        entregador.setNome("Entregador Teste");
        entregador.setEmail("entregador@test.com");
        entregador.setSenha("senha123");
        entregador.setTelefone("81999997777");
        entregador.setTipoUsuario(tipoEntregador);
        Usuario novoEntregador = usuarioService.criarUsuario(entregador);

        // 1. Registrar entrega
        Entrega entrega = new Entrega();
        entrega.setCodigoRastreio("BR123456789");
        entrega.setDescricao("Pacote teste");
        entrega.setCompartimento(novoCompartimento);
        entrega.setEntregador(novoEntregador);
        entrega.setDestinatario(novoUsuario);
        
        Entrega novaEntrega = entregaService.registrarEntrega(entrega);
        assertNotNull(novaEntrega.getId());
        assertEquals(StatusEntrega.ENTREGUE, novaEntrega.getStatus());
        assertNotNull(novaEntrega.getDataEntrega());

        // 2. Verificar se compartimento foi ocupado
        var compartimentoAtualizado = compartimentoService.buscarCompartimentoPorId(novoCompartimento.getId());
        assertTrue(compartimentoAtualizado.get().getOcupado());

        // 3. Registrar retirada
        Entrega entregaRetirada = entregaService.registrarRetirada(novaEntrega.getId(), 
            compartimentoAtualizado.get().getCodigoAcesso());
        assertEquals(StatusEntrega.RETIRADO, entregaRetirada.getStatus());
        assertNotNull(entregaRetirada.getDataRetirada());
    }

    @Test
    public void testFluxoCompletoReserva_Regressao() {
        // Teste de regressão: fluxo completo de reserva
        
        // Setup completo
        Condominio novoCondominio = condominioService.criarCondominio(condominio);
        armario.setCondominio(novoCondominio);
        Armario novoArmario = armarioService.criarArmario(armario);
        compartimento.setArmario(novoArmario);
        Compartimento novoCompartimento = compartimentoService.criarCompartimento(compartimento);
        Usuario novoUsuario = usuarioService.criarUsuario(usuario);

        // 1. Criar reserva
        Reserva reserva = new Reserva();
        reserva.setDataInicio(LocalDateTime.now().plusHours(1));
        reserva.setDataFim(LocalDateTime.now().plusHours(3));
        reserva.setObservacao("Reserva teste");
        reserva.setCompartimento(novoCompartimento);
        reserva.setUsuario(novoUsuario);
        
        Reserva novaReserva = reservaService.criarReserva(reserva);
        assertNotNull(novaReserva.getId());
        assertEquals(StatusReserva.CONFIRMADA, novaReserva.getStatus());

        // 2. Verificar se compartimento foi ocupado
        var compartimentoAtualizado = compartimentoService.buscarCompartimentoPorId(novoCompartimento.getId());
        assertTrue(compartimentoAtualizado.get().getOcupado());

        // 3. Cancelar reserva
        Reserva reservaCancelada = reservaService.cancelarReserva(novaReserva.getId());
        assertEquals(StatusReserva.CANCELADA, reservaCancelada.getStatus());

        // 4. Verificar se compartimento foi liberado
        var compartimentoLiberado = compartimentoService.buscarCompartimentoPorId(novoCompartimento.getId());
        assertFalse(compartimentoLiberado.get().getOcupado());
    }

    @Test
    public void testValidacoesBasicas_Regressao() {
        // Teste de regressão: validações básicas ainda funcionam
        
        // Validação de usuário nulo
        assertThrows(Exception.class, () -> {
            usuarioService.criarUsuario(null);
        });

        // Validação de email inválido
        Usuario usuarioEmailInvalido = new Usuario();
        usuarioEmailInvalido.setNome("Teste");
        usuarioEmailInvalido.setEmail("email-invalido");
        usuarioEmailInvalido.setSenha("senha123");
        usuarioEmailInvalido.setTelefone("81999998888");
        usuarioEmailInvalido.setTipoUsuario(tipoUsuario);
        
        assertThrows(Exception.class, () -> {
            usuarioService.criarUsuario(usuarioEmailInvalido);
        });

        // Validação de condomínio sem nome
        Condominio condominioSemNome = new Condominio();
        condominioSemNome.setEndereco("Rua Teste");
        
        assertThrows(Exception.class, () -> {
            condominioService.criarCondominio(condominioSemNome);
        });
    }
}
