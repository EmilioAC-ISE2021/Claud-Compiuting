package cc.sars.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // Or HttpStatus.BAD_REQUEST, depending on desired semantics
public class CcTaskCompletedException extends RuntimeException {
    public CcTaskCompletedException(String message) {
        super(message);
    }
}
