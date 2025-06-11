package br.com.unit.tokseg.armariointeligente.service;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;
import br.com.unit.tokseg.armariointeligente.util.ValidationUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    public void testValidateNotNull_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateNotNull("valor", "campo");
        });
    }

    @Test
    public void testValidateNotNull_Falha() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateNotNull(null, "campo");
        });
    }

    @Test
    public void testValidateNotBlank_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateNotBlank("valor", "campo");
        });
    }

    @Test
    public void testValidateNotBlank_FalhaNull() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateNotBlank(null, "campo");
        });
    }

    @Test
    public void testValidateNotBlank_FalhaVazio() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateNotBlank("", "campo");
        });
    }

    @Test
    public void testValidateNotBlank_FalhaEspacos() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateNotBlank("   ", "campo");
        });
    }

    @Test
    public void testValidateEmail_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateEmail("test@example.com");
        });
    }

    @Test
    public void testValidateEmail_Falha() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateEmail("email-invalido");
        });
    }

    /* @Test
    public void testValidatePhone_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validatePhone("(81) 99999-8888");
        });
        
        assertDoesNotThrow(() -> {
            ValidationUtils.validatePhone("81999998888");
        });
    }

    @Test
    public void testValidatePhone_Falha() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validatePhone("123");
        });
    } */

    @Test
    public void testValidateStringLength_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateStringLength("teste", "campo", 10);
        });
    }

    @Test
    public void testValidateStringLength_Falha() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateStringLength("texto muito longo", "campo", 5);
        });
    }

    @Test
    public void testValidateStringLengthRange_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validateStringLength("teste", "campo", 2, 10);
        });
    }

    @Test
    public void testValidateStringLengthRange_FalhaMuitoCurto() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateStringLength("a", "campo", 2, 10);
        });
    }

    @Test
    public void testValidateStringLengthRange_FalhaMuitoLongo() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateStringLength("texto muito longo", "campo", 2, 10);
        });
    }

    @Test
    public void testValidateDateRange_Sucesso() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.plusHours(2);
        
        assertDoesNotThrow(() -> {
            ValidationUtils.validateDateRange(inicio, fim);
        });
    }

    @Test
    public void testValidateDateRange_Falha() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.minusHours(2);
        
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateDateRange(inicio, fim);
        });
    }

    @Test
    public void testValidateFutureDate_Sucesso() {
        LocalDateTime futuro = LocalDateTime.now().plusHours(1);
        
        assertDoesNotThrow(() -> {
            ValidationUtils.validateFutureDate(futuro, "campo");
        });
    }

    @Test
    public void testValidateFutureDate_Falha() {
        LocalDateTime passado = LocalDateTime.now().minusHours(1);
        
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validateFutureDate(passado, "campo");
        });
    }

    @Test
    public void testValidatePositiveNumber_Sucesso() {
        assertDoesNotThrow(() -> {
            ValidationUtils.validatePositiveNumber(10, "campo");
        });
        
        assertDoesNotThrow(() -> {
            ValidationUtils.validatePositiveNumber(10.5, "campo");
        });
    }

    @Test
    public void testValidatePositiveNumber_Falha() {
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validatePositiveNumber(-5, "campo");
        });
        
        assertThrows(BadRequestException.class, () -> {
            ValidationUtils.validatePositiveNumber(0, "campo");
        });
    }
}
