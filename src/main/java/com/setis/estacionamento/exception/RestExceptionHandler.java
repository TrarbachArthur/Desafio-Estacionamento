package com.setis.estacionamento.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(GeneralException ex, HttpServletRequest request) {
        ExceptionResponse corpo = new ExceptionResponse(
                ex.getStatus().value(), ex.getCodigo(), ex.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(corpo);
    }

}
