package cc.sars.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SerieNotFoundException extends RuntimeException {

    public SerieNotFoundException(String message) {
        super(message);
    }
}
