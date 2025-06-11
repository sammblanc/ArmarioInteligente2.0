package br.com.unit.tokseg.armariointeligente.util;

import br.com.unit.tokseg.armariointeligente.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^$$?\\d{2}$$?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$");

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " não pode ser nulo");
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(fieldName + " não pode ser nulo ou vazio");
        }
    }

    public static void validateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Email deve ter um formato válido");
        }
    }

    /*public static void validatePhone(String phone) {
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException("Telefone deve ter um formato válido");
        }
    }
*/
    public static void validateStringLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BadRequestException(fieldName + " deve ter no máximo " + maxLength + " caracteres");
        }
    }

    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (value != null) {
            if (value.length() < minLength) {
                throw new BadRequestException(fieldName + " deve ter pelo menos " + minLength + " caracteres");
            }
            if (value.length() > maxLength) {
                throw new BadRequestException(fieldName + " deve ter no máximo " + maxLength + " caracteres");
            }
        }
    }

    public static void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Data de início não pode ser posterior à data de fim");
        }
    }

    public static void validateFutureDate(LocalDateTime date, String fieldName) {
        if (date != null && date.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(fieldName + " deve ser no futuro");
        }
    }

    public static void validatePositiveNumber(Number value, String fieldName) {
        if (value != null && value.doubleValue() <= 0) {
            throw new BadRequestException(fieldName + " deve ser um número positivo");
        }
    }
}
