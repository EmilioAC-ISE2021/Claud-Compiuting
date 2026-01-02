package cc.sars.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AssignmentForbiddenException extends RuntimeException {
    public AssignmentForbiddenException(String message) {
        super(message);
    }
}
