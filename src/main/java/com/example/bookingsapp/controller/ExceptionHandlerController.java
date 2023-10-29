package com.example.bookingsapp.controller;


import com.example.bookingsapp.exceptions.InvalidRequestException;
import com.example.bookingsapp.exceptions.NotFoundException;
import com.openapi.samples.gen.springbootserver.model.CommonError;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    public static final String CONSTRAINT_VIOLATION_ERROR_MESSAGE = "Constraint violation";

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonError> handleInvalidRequestException(InvalidRequestException exception) {
        return getResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonError> handleNotFoundException(NotFoundException exception) {
        return getResponse(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<CommonError> handleDataAccessException(DataAccessException exception) {
        return getResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, CONSTRAINT_VIOLATION_ERROR_MESSAGE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonError> handleRuntimeException(RuntimeException exception) {
        return getResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<CommonError> getResponse(RuntimeException exception, HttpStatus status, String message) {
        CommonError error = new CommonError()
                .error(exception.getClass().getSimpleName())
                .message(message);
        logger.error(error, exception);
        return new ResponseEntity<>(error, status);
    }

    private ResponseEntity<CommonError> getResponse(RuntimeException exception, HttpStatus status) {
        return getResponse(exception, status, exception.getMessage());
    }
}
