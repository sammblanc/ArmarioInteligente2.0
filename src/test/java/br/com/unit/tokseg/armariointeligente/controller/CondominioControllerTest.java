package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.model.Condominio;
import br.com.unit.tokseg.armariointeligente.service.CondominioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CondominioControllerTest {

    @Mock
    private CondominioService condominioService;

    @InjectMocks
    private CondominioController condominioController;

    private Condominio condominio;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        condominio = new Condominio();
        condominio.setId(1L);
        condominio.setNome("Residencial Teste");
        condominio.setEndereco("Rua Teste, 123");
        condominio.setCidade("Cidade Teste");
        condominio.setEstado("Estado Teste");
    }

    @Test
    public void testCriarCondominio() {
        when(condominioService.criarCondominio(any(Condominio.class))).thenReturn(condominio);

        ResponseEntity<?> response = condominioController.criarCondominio(condominio);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(condominio, response.getBody());
        verify(condominioService, times(1)).criarCondominio(any(Condominio.class));
    }

    @Test
    public void testListarCondominios() {
        List<Condominio> condominios = Arrays.asList(condominio);
        when(condominioService.listarCondominios()).thenReturn(condominios);

        ResponseEntity<?> response = condominioController.listarCondominios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(condominios, response.getBody());
        verify(condominioService, times(1)).listarCondominios();
    }

    @Test
    public void testBuscarCondominioPorId() {
        when(condominioService.buscarCondominioPorId(anyLong())).thenReturn(Optional.of(condominio));

        ResponseEntity<?> response = condominioController.buscarCondominioPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(condominio, response.getBody());
        verify(condominioService, times(1)).buscarCondominioPorId(anyLong());
    }

    @Test
    public void testAtualizarCondominio() {
        when(condominioService.atualizarCondominio(anyLong(), any(Condominio.class))).thenReturn(condominio);

        ResponseEntity<?> response = condominioController.atualizarCondominio(1L, condominio);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(condominio, response.getBody());
        verify(condominioService, times(1)).atualizarCondominio(anyLong(), any(Condominio.class));
    }

    @Test
    public void testDeletarCondominio() {
        doNothing().when(condominioService).deletarCondominio(anyLong());

        ResponseEntity<?> response = condominioController.deletarCondominio(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(condominioService, times(1)).deletarCondominio(anyLong());
    }
}
