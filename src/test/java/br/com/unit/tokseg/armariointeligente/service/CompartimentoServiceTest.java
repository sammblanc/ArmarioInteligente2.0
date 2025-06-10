package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.exception.ResourceNotFoundException;
import br.com.unit.tokseg.armariointeligente.model.Armario;
import br.com.unit.tokseg.armariointeligente.model.Compartimento;
import br.com.unit.tokseg.armariointeligente.repository.ArmarioRepository;
import br.com.unit.tokseg.armariointeligente.repository.CompartimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CompartimentoServiceTest {

    @Mock
    private CompartimentoRepository compartimentoRepository;

    @Mock
    private ArmarioRepository armarioRepository;

    @InjectMocks
    private CompartimentoService compartimentoService;

    private Armario armario;
    private Compartimento compartimento;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        armario = new Armario();
        armario.setId(1L);
        armario.setIdentificacao("ARM-001");

        compartimento = new Compartimento();
        compartimento.setId(1L);
        compartimento.setNumero("A1");
        compartimento.setTamanho("M");
        compartimento.setOcupado(false);
        compartimento.setCodigoAcesso("123456");
        compartimento.setArmario(armario);
    }

    @Test
    public void testCriarCompartimento() {
        when(armarioRepository.findById(anyLong())).thenReturn(Optional.of(armario));
        when(compartimentoRepository.findByNumeroAndArmarioId(anyString(), anyLong())).thenReturn(Optional.empty());
        when(compartimentoRepository.save(any(Compartimento.class))).thenReturn(compartimento);

        Compartimento resultado = compartimentoService.criarCompartimento(compartimento);

        assertNotNull(resultado);
        assertEquals(compartimento.getId(), resultado.getId());
        assertEquals(compartimento.getNumero(), resultado.getNumero());
        verify(compartimentoRepository, times(1)).save(any(Compartimento.class));
    }

    @Test
    public void testCriarCompartimentoSemNumero() {
        compartimento.setNumero(null);

        assertThrows(BadRequestException.class, () -> {
            compartimentoService.criarCompartimento(compartimento);
        });
    }

    @Test
    public void testListarCompartimentos() {
        List<Compartimento> compartimentos = Arrays.asList(compartimento);
        when(compartimentoRepository.findAll()).thenReturn(compartimentos);

        List<Compartimento> resultado = compartimentoService.listarCompartimentos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(compartimentoRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarCompartimentoPorId() {
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));

        Optional<Compartimento> resultado = compartimentoService.buscarCompartimentoPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(compartimento.getId(), resultado.get().getId());
        verify(compartimentoRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testAtualizarStatusCompartimento() {
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));
        when(compartimentoRepository.save(any(Compartimento.class))).thenReturn(compartimento);

        Compartimento resultado = compartimentoService.atualizarStatusCompartimento(1L, true);

        assertNotNull(resultado);
        assertTrue(resultado.getOcupado());
        verify(compartimentoRepository, times(1)).save(any(Compartimento.class));
    }

    @Test
    public void testGerarNovoCodigoAcesso() {
        when(compartimentoRepository.findById(anyLong())).thenReturn(Optional.of(compartimento));
        when(compartimentoRepository.save(any(Compartimento.class))).thenReturn(compartimento);

        Compartimento resultado = compartimentoService.gerarNovoCodigoAcesso(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigoAcesso());
        verify(compartimentoRepository, times(1)).save(any(Compartimento.class));
    }
}
