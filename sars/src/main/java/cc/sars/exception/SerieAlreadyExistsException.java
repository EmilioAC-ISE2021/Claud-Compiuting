package cc.sars.exception;

import cc.sars.exception.ResourceAlreadyExistsException;

public class SerieAlreadyExistsException extends ResourceAlreadyExistsException {
    public SerieAlreadyExistsException(String message) {
        super(message);
    }
}
