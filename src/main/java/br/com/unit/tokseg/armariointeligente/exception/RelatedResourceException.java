package br.com.unit.tokseg.armariointeligente.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RelatedResourceException extends RuntimeException {
    
    public RelatedResourceException(String message) {
        super(message);
    }
    
    public RelatedResourceException(String resourceName, String relatedResourceName) {
        super(String.format("Não é possível excluir %s pois existem %s vinculados a ele", resourceName, relatedResourceName));
    }
}
