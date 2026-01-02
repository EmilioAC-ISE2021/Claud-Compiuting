package cc.sars.controller.api.handler;

import cc.sars.controller.api.dto.ErrorResponse;
import cc.sars.exception.ResourceAlreadyExistsException;
import cc.sars.exception.SerieNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "cc.sars.controller.api")
public class RestExceptionHandler {

    @ExceptionHandler(SerieNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSerieNotFoundException(SerieNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
