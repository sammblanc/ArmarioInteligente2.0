package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import br.com.unit.tokseg.armariointeligente.repository.ReservaRepository;
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

public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private CompartimentoRepository compartimentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CompartimentoService compartimentoService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservaService reservaService;

    private Reserva reserva;
    private Compartimento compartimento;
    private Usuario usuario;
    private Armario armario;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        armario = new Armario();
        armario.setId(1L);
        armario.setIdentificacao("ARM-001");

        compartimento = new Compartimento();
        compartimento.setId(1L);
        compartimento.setNumero("A1");
        compartimento.setOcupado(false);
        compartimento.setCodigoAcesso("123456");
        compartimento.setArmario(armario);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Cliente Teste");
        usuario.setEmail("cliente@test.com");

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setDataInicio(LocalDateTime.now().plusHours(1));
        reserva.setDataFim(LocalDateTime.now().plusHours(3));
        reserva.setObservacao("Reserva teste");
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setCompartimento(compartimento);
        reserva.setUsuario(usuario);
    }

    @Test
    public void testCriarReserva_Sucesso() {
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.criarReserva(reserva);

        assertNotNull(resultado);
        assertEquals(StatusReserva.CONFIRMADA, resultado.getStatus());
        verify(compartimentoService, times(1)).atualizarStatusCompartimento(anyLong(), eq(true));
        verify(notificationService, times(1)).enviarNotificacaoReservaConfirmada(any(Reserva.class));
    }

    @Test
    public void testCriarReserva_ReservaNula() {
        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(null);
        });
    }

    @Test
    public void testCriarReserva_DataInicioNula() {
        reserva.setDataInicio(null);

        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCriarReserva_DataFimNula() {
        reserva.setDataFim(null);

        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCriarReserva_DataInicioNoPassado() {
        reserva.setDataInicio(LocalDateTime.now().minusHours(1));

        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCriarReserva_DataInicioAposDataFim() {
        reserva.setDataInicio(LocalDateTime.now().plusHours(3));
        reserva.setDataFim(LocalDateTime.now().plusHours(1));

        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCriarReserva_CompartimentoOcupado() {
        compartimento.setOcupado(true);
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));

        assertThrows(BadRequestException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCriarReserva_CompartimentoNaoEncontrado() {
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reservaService.criarReserva(reserva);
        });
    }

    @Test
    public void testCancelarReserva_Sucesso() {
        reserva.setStatus(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.cancelarReserva(1L);

        assertNotNull(resultado);
        assertEquals(StatusReserva.CANCELADA, resultado.getStatus());
        verify(compartimentoService, times(1)).atualizarStatusCompartimento(anyLong(), eq(false));
        verify(notificationService, times(1)).enviarNotificacaoReservaCancelada(any(Reserva.class));
    }

    @Test
    public void testCancelarReserva_ReservaNaoEncontrada() {
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reservaService.cancelarReserva(1L);
        });
    }

    @Test
    public void testCancelarReserva_StatusInvalido() {
        reserva.setStatus(StatusReserva.CONCLUIDA);
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reserva));

        assertThrows(BadRequestException.class, () -> {
            reservaService.cancelarReserva(1L);
        });
    }

    @Test
    public void testConcluirReserva_Sucesso() {
        reserva.setStatus(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.concluirReserva(1L);

        assertNotNull(resultado);
        assertEquals(StatusReserva.CONCLUIDA, resultado.getStatus());
        verify(compartimentoService, times(1)).atualizarStatusCompartimento(anyLong(), eq(false));
    }
}
