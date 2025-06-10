package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceAlreadyExistsException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import br.com.unit.tokseg.armariointeligente.repository.EntregaRepository;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EntregaServiceTest {

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private CompartimentoRepository compartimentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CompartimentoService compartimentoService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EntregaService entregaService;

    private Entrega entrega;
    private Compartimento compartimento;
    private Usuario entregador;
    private Usuario destinatario;
    private TipoUsuario tipoEntregador;
    private Armario armario;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        tipoEntregador = new TipoUsuario();
        tipoEntregador.setNome("Entregador");

        armario = new Armario();
        armario.setId(1L);
        armario.setIdentificacao("ARM-001");

        compartimento = new Compartimento();
        compartimento.setId(1L);
        compartimento.setNumero("A1");
        compartimento.setOcupado(false);
        compartimento.setCodigoAcesso("123456");
        compartimento.setArmario(armario);

        entregador = new Usuario();
        entregador.setId(1L);
        entregador.setNome("Entregador Teste");
        entregador.setEmail("entregador@test.com");
        entregador.setTipoUsuario(tipoEntregador);

        destinatario = new Usuario();
        destinatario.setId(2L);
        destinatario.setNome("Cliente Teste");
        destinatario.setEmail("cliente@test.com");

        entrega = new Entrega();
        entrega.setId(1L);
        entrega.setCodigoRastreio("BR123456789");
        entrega.setDescricao("Pacote teste");
        entrega.setStatus(StatusEntrega.AGUARDANDO_ENTREGA);
        entrega.setCompartimento(compartimento);
        entrega.setEntregador(entregador);
        entrega.setDestinatario(destinatario);
    }

    @Test
    public void testRegistrarEntrega_Sucesso() {
        when(entregaRepository.findByCodigoRastreio(anyString())).thenReturn(Optional.empty());
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entregador));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(entregaRepository.save(any(Entrega.class))).thenReturn(entrega);

        Entrega resultado = entregaService.registrarEntrega(entrega);

        assertNotNull(resultado);
        assertEquals(StatusEntrega.ENTREGUE, resultado.getStatus());
        verify(compartimentoService, times(1)).atualizarStatusCompartimento(anyLong(), eq(true));
        verify(notificationService, times(1)).enviarNotificacaoEntregaRealizada(any(Entrega.class));
    }

    @Test
    public void testRegistrarEntrega_EntregaNula() {
        assertThrows(BadRequestException.class, () -> {
            entregaService.registrarEntrega(null);
        });
    }

    @Test
    public void testRegistrarEntrega_CodigoRastreioNulo() {
        entrega.setCodigoRastreio(null);

        assertThrows(BadRequestException.class, () -> {
            entregaService.registrarEntrega(entrega);
        });
    }

    @Test
    public void testRegistrarEntrega_CodigoRastreioJaExiste() {
        when(entregaRepository.findByCodigoRastreio(anyString())).thenReturn(Optional.of(entrega));

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            entregaService.registrarEntrega(entrega);
        });
    }

    @Test
    public void testRegistrarEntrega_CompartimentoOcupado() {
        compartimento.setOcupado(true);
        when(entregaRepository.findByCodigoRastreio(anyString())).thenReturn(Optional.empty());
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));

        assertThrows(BadRequestException.class, () -> {
            entregaService.registrarEntrega(entrega);
        });
    }

    @Test
    public void testRegistrarEntrega_CompartimentoNaoEncontrado() {
        when(entregaRepository.findByCodigoRastreio(anyString())).thenReturn(Optional.empty());
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            entregaService.registrarEntrega(entrega);
        });
    }

    @Test
    public void testRegistrarRetirada_Sucesso() {
        entrega.setStatus(StatusEntrega.ENTREGUE);
        when(entregaRepository.findById(anyLong())).thenReturn(Optional.of(entrega));
        when(entregaRepository.save(any(Entrega.class))).thenReturn(entrega);

        Entrega resultado = entregaService.registrarRetirada(1L, "123456");

        assertNotNull(resultado);
        assertEquals(StatusEntrega.RETIRADO, resultado.getStatus());
        assertNotNull(resultado.getDataRetirada());
        verify(compartimentoService, times(1)).atualizarStatusCompartimento(anyLong(), eq(false));
        verify(compartimentoService, times(1)).gerarNovoCodigoAcesso(anyLong());
        verify(notificationService, times(1)).enviarNotificacaoEncomendaRetirada(any(Entrega.class));
    }

    @Test
    public void testRegistrarRetirada_EntregaNaoEncontrada() {
        when(entregaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            entregaService.registrarRetirada(1L, "123456");
        });
    }

    @Test
    public void testRegistrarRetirada_StatusInvalido() {
        entrega.setStatus(StatusEntrega.RETIRADO);
        when(entregaRepository.findById(anyLong())).thenReturn(Optional.of(entrega));

        assertThrows(BadRequestException.class, () -> {
            entregaService.registrarRetirada(1L, "123456");
        });
    }

    @Test
    public void testRegistrarRetirada_CodigoAcessoInvalido() {
        entrega.setStatus(StatusEntrega.ENTREGUE);
        when(entregaRepository.findById(anyLong())).thenReturn(Optional.of(entrega));

        assertThrows(BadRequestException.class, () -> {
            entregaService.registrarRetirada(1L, "654321");
        });
    }

    @Test
    public void testCancelarEntrega_Sucesso() {
        entrega.setStatus(StatusEntrega.ENTREGUE);
        when(entregaRepository.findById(anyLong())).thenReturn(Optional.of(entrega));
        when(entregaRepository.save(any(Entrega.class))).thenReturn(entrega);

        Entrega resultado = entregaService.cancelarEntrega(1L);

        assertNotNull(resultado);
        assertEquals(StatusEntrega.CANCELADO, resultado.getStatus());
    }

    @Test
    public void testBuscarEntregaPorCodigoRastreio() {
        when(entregaRepository.findByCodigoRastreio(anyString())).thenReturn(Optional.of(entrega));

        Optional<Entrega> resultado = entregaService.buscarEntregaPorCodigoRastreio("BR123456789");

        assertTrue(resultado.isPresent());
        assertEquals(entrega.getCodigoRastreio(), resultado.get().getCodigoRastreio());
    }
}
